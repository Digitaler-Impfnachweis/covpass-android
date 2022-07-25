/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.revocation

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.commonapp.isBeforeUpdateInterval
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.revocation.RevocationListUpdateTable
import de.rki.covpass.sdk.utils.parallelMap
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

public class RevocationListUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val revocationLocalListRepository: RevocationLocalListRepository = sdkDeps.revocationLocalListRepository,
) : BaseReactiveState<BaseEvents>(scope) {

    public var offlineModeState: Boolean
        get() = revocationLocalListRepository.revocationListUpdateIsOn.value
        set(value) {
            launch {
                revocationLocalListRepository.revocationListUpdateIsOn.set(value)
            }
        }

    public fun update() {
        if (!revocationLocalListRepository.revocationListUpdateIsOn.value) return
        launch {
            if (revocationLocalListRepository.lastRevocationUpdateFinish.value.isBeforeUpdateInterval()) {
                updateRevocationList()
            }
        }
    }

    private suspend fun updateRevocationList() {
        if (isUpdateResetNeeded()) {
            revocationLocalListRepository.lastRevocationUpdatedTable.set(RevocationListUpdateTable.COMPLETED)
        }
        revocationLocalListRepository.updateLastStartRevocationValidation(Instant.now())

        do {
            when (revocationLocalListRepository.lastRevocationUpdatedTable.value) {
                RevocationListUpdateTable.COMPLETED -> {
                    // Update kidlist
                    revocationLocalListRepository.updateKidList()
                    revocationLocalListRepository.lastRevocationUpdatedTable.set(RevocationListUpdateTable.KID_LIST)
                }
                RevocationListUpdateTable.KID_LIST -> {
                    // update index
                    revocationLocalListRepository.updateIndex()
                    revocationLocalListRepository.lastRevocationUpdatedTable.set(RevocationListUpdateTable.INDEX)
                }
                RevocationListUpdateTable.INDEX -> {
                    // update byte one
                    updateByteOne()
                    revocationLocalListRepository.lastRevocationUpdatedTable.set(RevocationListUpdateTable.BYTE_ONE)
                }
                RevocationListUpdateTable.BYTE_ONE -> {
                    // update byte two
                    updateByteTwo()
                    revocationLocalListRepository.lastRevocationUpdatedTable.set(RevocationListUpdateTable.BYTE_TWO)
                }
                RevocationListUpdateTable.BYTE_TWO -> {
                    revocationLocalListRepository.updateLastRevocationValidation(
                        revocationLocalListRepository.lastRevocationUpdateStart.value
                    )
                    revocationLocalListRepository.lastRevocationUpdatedTable.set(RevocationListUpdateTable.COMPLETED)
                }
            }
        } while (revocationLocalListRepository.lastRevocationUpdatedTable.value != RevocationListUpdateTable.COMPLETED)
    }

    private fun isUpdateResetNeeded(): Boolean =
        revocationLocalListRepository.lastRevocationUpdateStart.value.isAfter(
            revocationLocalListRepository.lastRevocationUpdateFinish.value
        ) && revocationLocalListRepository.lastRevocationUpdateStart.value.isBeforeUpdateInterval()

    private suspend fun updateByteOne() {
        val oldByteOneList = revocationLocalListRepository.getSavedByteOneList()
        val indexList = revocationLocalListRepository.getSavedIndexList()

        indexList.forEach {
            val filteredOldByteOneList = oldByteOneList.filter { byteOneLocal ->
                byteOneLocal.kid.contentEquals(it.kid) && byteOneLocal.hashVariant == it.hashVariant
            }
            revocationLocalListRepository.deleteOldByteOneList(filteredOldByteOneList, it)

            it.index.toList().parallelMap { (byteOne, revocationIndexEntry) ->
                revocationLocalListRepository.updateByteOne(revocationIndexEntry, it, byteOne)
            }
        }
    }

    private suspend fun updateByteTwo() {
        val oldByteTwoList = revocationLocalListRepository.getSavedByteTwoList()
        val indexList = revocationLocalListRepository.getSavedIndexList()

        indexList.forEach {
            val filteredOldByteTwoList = oldByteTwoList.filter { byteTwoLocal ->
                byteTwoLocal.kid.contentEquals(it.kid) && byteTwoLocal.hashVariant == it.hashVariant
            }
            revocationLocalListRepository.deleteOldByteTwoList(filteredOldByteTwoList, it)

            it.index.toList().parallelMap { (byteOne, revocationIndexEntry) ->
                revocationIndexEntry.byte2?.forEach { byte2Entry ->
                    revocationLocalListRepository.updateByteTwoLogic(
                        byte2Entry,
                        it,
                        byteOne
                    )
                }
            }
        }
    }
}

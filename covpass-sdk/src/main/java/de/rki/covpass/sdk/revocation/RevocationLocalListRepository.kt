/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

import COSE.OneKey
import COSE.Sign1Message
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.upokecenter.cbor.CBORObject
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.revocation.database.*
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.DscRepository.Companion.NO_UPDATE_YET
import de.rki.covpass.sdk.utils.isNetworkError
import de.rki.covpass.sdk.utils.toHex
import de.rki.covpass.sdk.utils.toRFC1123OrEmpty
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.security.PublicKey
import java.time.Instant

public class RevocationLocalListRepository(
    httpClient: HttpClient,
    host: String,
    store: CborSharedPrefsStore,
    private val database: RevocationDatabase,
    private val revocationListPublicKey: PublicKey
) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(defaultJson)
        }
    }

    public val revocationListUpdateIsOn: SuspendMutableValueFlow<Boolean> =
        store.getData("revocation_list_update_is_on", false)

    public val lastRevocationUpdateFinish: SuspendMutableValueFlow<Instant> =
        store.getData("last_revocation_update", NO_UPDATE_YET)

    public val lastRevocationUpdateStart: SuspendMutableValueFlow<Instant> =
        store.getData("last_revocation_update_start", NO_UPDATE_YET)

    public val lastRevocationUpdatedTable: SuspendMutableValueFlow<RevocationListUpdateTable> =
        store.getData("last_revocation_update_TABLE", RevocationListUpdateTable.COMPLETED)

    public suspend fun getSavedKidList(): List<RevocationKidLocal> = database.revocationKidDao().getAll()

    public suspend fun getSavedIndexList(): List<RevocationIndexLocal> = database.revocationIndexDao().getAll()

    public suspend fun getSavedIndex(kid: ByteArray, hashType: Byte): Map<Byte, RevocationIndexEntry> =
        database.revocationIndexDao().getIndex(kid, hashType).index

    public suspend fun getSavedByteOneList(): List<RevocationByteOneLocal> = database.revocationByteOneDao().getAll()

    public suspend fun getSavedByteOneChunk(kid: ByteArray, hashType: Byte, byte1: Byte): List<ByteArray> =
        database.revocationByteOneDao().getByteOneChunks(kid, hashType, byte1)?.chunks ?: emptyList()

    public suspend fun getSavedByteTwoList(): List<RevocationByteTwoLocal> = database.revocationByteTwoDao().getAll()

    public suspend fun getSavedByteTwoChunk(
        kid: ByteArray,
        hashType: Byte,
        byte1: Byte,
        byte2: Byte
    ): List<ByteArray> =
        database.revocationByteTwoDao().getByteTwoChunks(kid, hashType, byte1, byte2)?.chunks ?: emptyList()

    /**
     * Update the [lastRevocationUpdateFinish].
     */
    public suspend fun updateLastRevocationValidation(instant: Instant) {
        lastRevocationUpdateFinish.set(instant)
    }

    /**
     * Update the [lastRevocationUpdateStart].
     */
    public suspend fun updateLastStartRevocationValidation(instant: Instant) {
        lastRevocationUpdateStart.set(instant)
    }

    public suspend fun updateKidList() {
        val newKidListObject = getKidList(
            lastRevocationUpdateFinish.value
        )
        if (newKidListObject.wasModifiedSince) {
            val oldKidListObject = database.revocationKidDao().getAll()
            val removedKidList = oldKidListObject.filter { oldElement ->
                !newKidListObject.kidList.map { it.kid }.contains(oldElement.kid)
            }
            database.revocationKidDao().replaceAll(
                newKidListObject.kidList.map {
                    RevocationKidLocal(
                        it.kid,
                        it.hashVariants
                    )
                }
            )
            // remove deleted kid from other tables
            removedKidList.forEach {
                database.revocationIndexDao().deleteAllFromKid(it.kid)
                database.revocationByteOneDao().deleteAllFromKid(it.kid)
                database.revocationByteTwoDao().deleteAllFromKid(it.kid)
            }
        }
    }

    public suspend fun updateIndex() {
        val oldIndexList = database.revocationIndexDao().getAll()
        val kidList = database.revocationKidDao().getAll()
        kidList.forEach { revocationKidLocal ->
            val filteredOldIndexList = oldIndexList.filter { it.kid.contentEquals(revocationKidLocal.kid) }
            filteredOldIndexList.forEach {
                if (revocationKidLocal.hashVariants[it.hashVariant] == null) {
                    database.revocationIndexDao().deleteElement(it.kid, it.hashVariant)
                    database.revocationByteOneDao().deleteAllFromKidAndHashVariant(it.kid, it.hashVariant)
                    database.revocationByteTwoDao().deleteAllFromKidAndHashVariant(it.kid, it.hashVariant)
                }
            }
            revocationKidLocal.hashVariants.forEach { (hashType, _) ->
                val index = getIndex(revocationKidLocal.kid, hashType, lastRevocationUpdateFinish.value)
                if (index.wasModifiedSince) {
                    database.revocationIndexDao().insertIndex(
                        RevocationIndexLocal(
                            revocationKidLocal.kid,
                            hashType,
                            index.indexList
                        )
                    )
                }
            }
        }
    }

    public suspend fun updateByteOne(
        revocationIndexEntry: RevocationIndexEntry,
        revocationIndexLocal: RevocationIndexLocal,
        byteOne: Byte
    ) {
        if (
            Instant.ofEpochSecond(revocationIndexEntry.timestamp ?: 0)
                .isAfter(lastRevocationUpdateFinish.value) &&
            lastRevocationUpdateFinish.value.epochSecond >=
            (
                database.revocationByteOneDao().getByteOneChunks(
                    revocationIndexLocal.kid,
                    revocationIndexLocal.hashVariant,
                    byteOne
                )?.timestamp ?: 0
                )
        ) {
            val byte1ChunkList = getByteOneChunk(
                revocationIndexLocal.kid,
                revocationIndexLocal.hashVariant,
                byteOne,
                lastRevocationUpdateFinish.value
            )
            database.revocationByteOneDao().insertByteOne(
                RevocationByteOneLocal(
                    revocationIndexLocal.kid,
                    revocationIndexLocal.hashVariant,
                    byteOne,
                    byte1ChunkList.chunkList,
                    lastRevocationUpdateStart.value.epochSecond
                )
            )
        }
    }

    public suspend fun updateByteTwoLogic(
        byte2Entry: Map.Entry<Byte, RevocationIndexByte2Entry>,
        indexEntry: RevocationIndexLocal,
        byteOne: Byte
    ) {
        if (
            Instant.ofEpochSecond(byte2Entry.value.timestamp ?: 0)
                .isAfter(lastRevocationUpdateFinish.value) &&
            lastRevocationUpdateFinish.value.epochSecond >=
            (
                database.revocationByteTwoDao().getByteTwoChunks(
                    indexEntry.kid,
                    indexEntry.hashVariant,
                    byteOne,
                    byte2Entry.key,
                )?.timestamp ?: 0
                )
        ) {
            val byte2ChunkList = getByteTwoChunk(
                indexEntry.kid,
                indexEntry.hashVariant,
                byteOne,
                byte2Entry.key,
                lastRevocationUpdateFinish.value
            )
            database.revocationByteTwoDao().insertByteTwo(
                RevocationByteTwoLocal(
                    indexEntry.kid,
                    indexEntry.hashVariant,
                    byteOne,
                    byte2Entry.key,
                    byte2ChunkList.chunkList,
                    lastRevocationUpdateStart.value.epochSecond
                )
            )
        }
    }

    public suspend fun deleteOldByteOneList(
        filteredOldByteOneList: List<RevocationByteOneLocal>,
        revocationIndexLocal: RevocationIndexLocal
    ) {
        filteredOldByteOneList.forEach { byteOneLocal ->
            if (revocationIndexLocal.index[byteOneLocal.byteOne] == null) {
                database.revocationByteOneDao().deleteElement(
                    revocationIndexLocal.kid,
                    revocationIndexLocal.hashVariant,
                    byteOneLocal.byteOne
                )
                database.revocationByteTwoDao().deleteAllFromKidAndHashVariantAndByteOne(
                    revocationIndexLocal.kid,
                    revocationIndexLocal.hashVariant,
                    byteOneLocal.byteOne
                )
            }
        }
    }

    public suspend fun deleteOldByteTwoList(
        filteredOldByteTwoList: List<RevocationByteTwoLocal>,
        revocationIndexLocal: RevocationIndexLocal
    ) {
        filteredOldByteTwoList.forEach { byteTwoLocal ->
            if (revocationIndexLocal.index[byteTwoLocal.byteOne]?.byte2?.get(byteTwoLocal.byteTwo) == null) {
                database.revocationByteTwoDao().deleteAllFromKidAndHashVariantAndByteOneAndByteTwo(
                    revocationIndexLocal.kid,
                    revocationIndexLocal.hashVariant,
                    byteTwoLocal.byteOne,
                    byteTwoLocal.byteTwo
                )
            }
        }
    }

    public suspend fun getKidList(lastUpdate: Instant): KidListObject {
        val signedObject = getSigned("kid.lst", lastUpdate)
        return KidListObject(
            signedObject?.wasModifiedSince ?: false,
            signedObject?.cborObject?.toKidList() ?: emptyList()
        )
    }

    public suspend fun getIndex(kid: ByteArray, hashType: Byte, lastUpdate: Instant): IndexListObject {
        val signedObject = getSigned("${kid.toHex()}${hashType.toHex()}/index.lst", lastUpdate)
        return IndexListObject(
            signedObject?.wasModifiedSince ?: false,
            signedObject?.cborObject?.toIndexResponse() ?: emptyMap()
        )
    }

    public suspend fun getByteOneChunk(
        kid: ByteArray,
        hashType: Byte,
        byte1: Byte,
        lastUpdate: Instant
    ): ChunkListObject {
        val signedObject = getSigned("${kid.toHex()}${hashType.toHex()}/${byte1.toHex()}/chunk.lst", lastUpdate)
        return ChunkListObject(
            wasModifiedSince = signedObject?.wasModifiedSince ?: false,
            chunkList = signedObject?.cborObject?.toListOfByteArrays() ?: emptyList()
        )
    }

    public suspend fun getByteTwoChunk(
        kid: ByteArray,
        hashType: Byte,
        byte1: Byte,
        byte2: Byte,
        lastUpdate: Instant
    ): ChunkListObject {
        val signedObject =
            getSigned("${kid.toHex()}${hashType.toHex()}/${byte1.toHex()}/${byte2.toHex()}/chunk.lst", lastUpdate)
        return ChunkListObject(
            wasModifiedSince = signedObject?.wasModifiedSince ?: false,
            chunkList = signedObject?.cborObject?.toListOfByteArrays() ?: emptyList()
        )
    }

    private suspend fun getSigned(url: String, lastUpdate: Instant): SignedObject? {
        return try {
            val httpResponse: HttpResponse = client.get(url) {
                header("If-Modified-Since", lastUpdate.toRFC1123OrEmpty())
            }
            val list: ByteArray = httpResponse.receive()
            val sign1Message = Sign1Message.DecodeFromBytes(list) as Sign1Message
            validateRevocationSignature(sign1Message)
            SignedObject(
                wasModifiedSince = httpResponse.status == HttpStatusCode.OK,
                cborObject = CBORObject.DecodeFromBytes(sign1Message.GetContent())
            )
        } catch (e: RevocationRemoteListRepository.RevocationListSignatureValidationFailedException) {
            null
        } catch (e: Throwable) {
            if (isNetworkError(e)) {
                null
            } else {
                throw e
            }
        }
    }

    public fun validateRevocationSignature(sign1Message: Sign1Message) {
        if (!sign1Message.validate(OneKey(revocationListPublicKey, null))) {
            throw RevocationRemoteListRepository.RevocationListSignatureValidationFailedException()
        }
    }

    private data class SignedObject(
        val wasModifiedSince: Boolean = false,
        val cborObject: CBORObject?
    )

    public data class ChunkListObject(
        val wasModifiedSince: Boolean = false,
        val chunkList: List<ByteArray>
    )

    public data class IndexListObject(
        val wasModifiedSince: Boolean = false,
        val indexList: Map<Byte, RevocationIndexEntry>
    )

    public data class KidListObject(
        val wasModifiedSince: Boolean = false,
        val kidList: List<RevocationKidEntry>
    )
}

public enum class RevocationListUpdateTable {
    COMPLETED,
    KID_LIST,
    INDEX,
    BYTE_ONE,
    BYTE_TWO,
}

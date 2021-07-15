/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.models.DscList
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.dependencies.sdkDeps
import java.time.Instant

public const val DSC_UPDATE_INTERVAL_HOURS: Long = 24

/**
 * @return True, if the [lastUpdate] is not longer than [DSC_UPDATE_INTERVAL_HOURS] ago, else false.
 */
public fun isDscListUpToDate(lastUpdate: Instant): Boolean {
    val dscUpdateIntervalSeconds = DSC_UPDATE_INTERVAL_HOURS * 60 * 60
    return lastUpdate.isAfter(Instant.now().minusSeconds(dscUpdateIntervalSeconds))
}

/**
 * [CoroutineWorker] for updating the [DscList] periodically.
 */
public class DscListWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result =
        try {
            val result = sdkDeps.dscListService.getTrustedList()
            val dscList = sdkDeps.decoder.decodeDscList(result)
            sdkDeps.validator.updateTrustedCerts(dscList.toTrustedCerts())
            sdkDeps.dscRepository.updateDscList(dscList)
            Result.success()
        } catch (e: Throwable) {
            Lumber.e(e)
            Result.retry()
        }
}

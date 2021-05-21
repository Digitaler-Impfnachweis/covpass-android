/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.android.cert.toTrustedCerts
import de.rki.covpass.sdk.android.dependencies.sdkDeps

public class DscListUpdater(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result =
        try {
            val result = sdkDeps.dscListService.getTrustedList()
            val dscList = sdkDeps.decoder.decodeDscList(result)
            sdkDeps.validator.updateTrustedCerts(dscList.toTrustedCerts())
            commonDeps.dscRepository.updateDscList(dscList)
            Result.success()
        } catch (e: Throwable) {
            Lumber.e(e)
            Result.retry()
        }
}

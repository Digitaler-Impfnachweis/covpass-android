/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.ExperimentalHCertApi

public class ValueSetsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    @ExperimentalHCertApi
    override suspend fun doWork(): Result =
        try {
            sdkDeps.valueSetsRepository.loadValueSets()
            Result.success()
        } catch (e: Throwable) {
            Lumber.e(e)
            Result.retry()
        }
}

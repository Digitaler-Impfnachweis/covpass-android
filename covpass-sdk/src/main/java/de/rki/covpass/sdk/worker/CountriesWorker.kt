/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.dependencies.sdkDeps

@OptIn(DependencyAccessor::class)
public class CountriesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result =
        try {
            sdkDeps.covPassCountriesRepository.loadCountries()
            Result.success()
        } catch (e: Throwable) {
            Lumber.e(e)
            Result.retry()
        }
}

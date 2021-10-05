package de.rki.covpass.commonapp.utils

import androidx.work.*
import de.rki.covpass.sdk.worker.DSC_UPDATE_INTERVAL_HOURS
import java.util.concurrent.TimeUnit

public inline fun <reified T : ListenableWorker> WorkManager.schedulePeriodicWorker(
    tag: String,
    intervalInHours: Long = DSC_UPDATE_INTERVAL_HOURS
): Operation =
    this.enqueueUniquePeriodicWork(
        tag,
        ExistingPeriodicWorkPolicy.KEEP,
        PeriodicWorkRequestBuilder<T>(intervalInHours, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
    )

/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp

import androidx.work.WorkManager
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.checkapp.errorhandling.ErrorHandler
import de.rki.covpass.commonapp.CommonApplication
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.utils.schedulePeriodicWorker
import de.rki.covpass.sdk.worker.RulesWorker
import de.rki.covpass.sdk.worker.ValueSetsWorker

/**
 * Application class of CovPass Check.
 */
@OptIn(DependencyAccessor::class)
public class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
        start()
    }

    override fun initializeWorkManager(workManager: WorkManager) {
        super.initializeWorkManager(workManager)
        workManager.apply {
            schedulePeriodicWorker<RulesWorker>("rulesWorker")
            schedulePeriodicWorker<ValueSetsWorker>("valueSetsWorker")
        }
    }
}

/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app

import androidx.work.WorkManager
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.app.dependencies.CovpassDependencies
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.errorhandling.ErrorHandler
import de.rki.covpass.commonapp.CommonApplication
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.utils.schedulePeriodicWorker
import de.rki.covpass.sdk.worker.BoosterRulesWorker
import de.rki.covpass.sdk.worker.RulesWorker
import de.rki.covpass.sdk.worker.ValueSetsWorker

/**
 * Application class which defines dependencies for the Covpass App
 */
@OptIn(DependencyAccessor::class)
internal class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        covpassDeps = object : CovpassDependencies() {}
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
        start()
    }

    override fun initializeWorkManager(workManager: WorkManager) {
        super.initializeWorkManager(workManager)
        workManager.apply {
            schedulePeriodicWorker<BoosterRulesWorker>("boosterRulesWorker")
            schedulePeriodicWorker<RulesWorker>("rulesWorker")
            schedulePeriodicWorker<ValueSetsWorker>("valueSetsWorker")
        }
    }
}

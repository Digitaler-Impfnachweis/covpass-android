/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.cert.CovPassRulesRemoteDataSource
import de.rki.covpass.sdk.rules.CovPassEuRulesRepository
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRulesLocalDataSource
import de.rki.covpass.sdk.rules.remote.rules.CovPassRuleRemote
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test

internal class BoosterCertLogicEngineTest {

    @Test
    fun `test empty rule repository`() {
        val remoteDataSource: CovPassRulesRemoteDataSource = mockk()
        val localDataSourceEu: CovPassEuRulesLocalDataSource = mockk()
        val covPassRuleRemote: CovPassRuleRemote = mockk()
        val rulesUpdateRepository: RulesUpdateRepository = mockk(relaxed = true)

        coEvery { remoteDataSource.getRuleIdentifiers() } returns emptyList()
        coEvery { remoteDataSource.getRule("", "") } returns covPassRuleRemote
        coEvery { localDataSourceEu.getAllRules() } returns emptyList()
        coEvery { localDataSourceEu.replaceRules(any(), any()) } just Runs

        val repository = CovPassEuRulesRepository(
            remoteDataSource,
            localDataSourceEu,
            rulesUpdateRepository
        )
        runBlockingTest {
            repository.loadRules()
        }

        coVerify { remoteDataSource.getRuleIdentifiers() }
        coVerify { localDataSourceEu.getAllRules() }
        coVerify { localDataSourceEu.replaceRules(any(), any()) }
    }
}

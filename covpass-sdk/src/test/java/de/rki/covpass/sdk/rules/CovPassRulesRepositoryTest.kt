/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.cert.CovPassRulesRemoteDataSource
import de.rki.covpass.sdk.rules.local.rules.CovPassRulesLocalDataSource
import de.rki.covpass.sdk.rules.remote.rules.CovPassRuleRemote
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test

internal class CovPassRulesRepositoryTest {

    @Test
    fun `test empty rule repository`() {
        val remoteDataSource: CovPassRulesRemoteDataSource = mockk()
        val localDataSource: CovPassRulesLocalDataSource = mockk()
        val covPassRuleRemote: CovPassRuleRemote = mockk()
        val rulesUpdateRepository: RulesUpdateRepository = mockk(relaxed = true)

        coEvery { remoteDataSource.getRuleIdentifiers() } returns emptyList()
        coEvery { remoteDataSource.getRule("", "") } returns covPassRuleRemote
        coEvery { localDataSource.getAllCovPassRules() } returns emptyList()
        coEvery { localDataSource.replaceRules(any(), any()) } just Runs

        val repository = CovPassRulesRepository(
            remoteDataSource,
            localDataSource,
            rulesUpdateRepository
        )
        runBlockingTest {
            repository.loadRules()
        }

        coVerify { remoteDataSource.getRuleIdentifiers() }
        coVerify { localDataSource.getAllCovPassRules() }
        coVerify { localDataSource.replaceRules(any(), any()) }
    }
}

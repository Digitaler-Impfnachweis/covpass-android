/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.domain.rules

import de.rki.covpass.sdk.rules.CovPassRulesRepository
import dgca.verifier.app.engine.data.CertificateType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import java.time.ZonedDateTime
import kotlin.test.Test

internal class CovPassGetRulesUseCaseTest {

    @Test
    fun `test with empty empty issuanceCountryIsoCode`() {
        val repository: CovPassRulesRepository = mockk()

        coEvery {
            repository.getCovPassRulesBy(any(), any(), any(), any())
        } returns emptyList()

        val covPassGetRulesUseCase = CovPassGetRulesUseCase(repository)

        runBlockingTest {
            covPassGetRulesUseCase.invoke(
                "de",
                "",
                CertificateType.VACCINATION,
                ZonedDateTime.now()
            )
        }

        coVerify(exactly = 1) {
            repository.getCovPassRulesBy(any(), any(), any(), any())
        }
    }

    @Test
    fun `test with issuanceCountryIsoCode`() {
        val repository: CovPassRulesRepository = mockk()

        coEvery {
            repository.getCovPassRulesBy(any(), any(), any(), any())
        } returns emptyList()

        val covPassGetRulesUseCase = CovPassGetRulesUseCase(repository)

        runBlockingTest {
            covPassGetRulesUseCase.invoke(
                "de",
                "de",
                CertificateType.VACCINATION,
                ZonedDateTime.now()
            )
        }

        coVerify(exactly = 2) {
            repository.getCovPassRulesBy(any(), any(), any(), any())
        }
    }
}

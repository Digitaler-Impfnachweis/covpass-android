/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster

import de.rki.covpass.sdk.cert.BoosterCertLogicEngine
import de.rki.covpass.sdk.cert.BoosterRulesValidator
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Name
import de.rki.covpass.sdk.cert.models.Vaccination
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

internal class BoosterRulesValidatorTest {

    private val name1 = "Hans"
    private val date1 = "2021-01-01"
    private val idIncomplete1 = "certIncomplete1"
    private val vaccinationsIncomplete1 =
        listOf(Vaccination(doseNumber = 1, totalSerialDoses = 2, id = idIncomplete1))
    private val covCertificate = CovCertificate(
        name = Name(familyNameTransliterated = name1),
        birthDate = date1,
        vaccinations = vaccinationsIncomplete1,
        validUntil = Instant.now()
    )

    @Test
    fun `test empty booster rule repository`() = runTest {
        val boosterCertLogicEngine: BoosterCertLogicEngine = mockk()
        val boosterRulesRepository: CovPassBoosterRulesRepository = mockk()

        coEvery {
            boosterRulesRepository.getCovPassBoosterRulesBy(any(), any())
        } returns emptyList()
        coEvery {
            boosterCertLogicEngine.validate(
                any(), any(), any(), any()
            )
        } returns emptyList()

        val validator = BoosterRulesValidator(
            boosterCertLogicEngine,
            boosterRulesRepository
        )

        val results = validator.validate(covCertificate)

        assertTrue(results.isEmpty())
        coVerify { boosterRulesRepository.getCovPassBoosterRulesBy(any(), any()) }
        coVerify { boosterCertLogicEngine.validate(any(), any(), any(), any()) }
    }
}

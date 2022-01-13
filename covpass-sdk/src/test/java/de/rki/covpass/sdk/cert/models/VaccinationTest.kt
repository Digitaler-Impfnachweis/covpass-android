/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class VaccinationTest {
    private val vaccinationIncomplete = Vaccination(doseNumber = 1, totalSerialDoses = 2)
    private val vaccinationComplete = Vaccination(doseNumber = 2, totalSerialDoses = 2)
    private val vaccinationCompleteWithFullProtection =
        Vaccination(doseNumber = 2, totalSerialDoses = 2, occurrence = LocalDate.parse("2021-12-20"))
    private val vaccinationCompleteAfterRecovery =
        Vaccination(doseNumber = 1, totalSerialDoses = 1, product = "EU/1/20/1528")
    private val vaccinationFirstBooster = Vaccination(doseNumber = 2, totalSerialDoses = 1)
    private val vaccinationSecondBooster = Vaccination(doseNumber = 4, totalSerialDoses = 2)
    private val vaccinationThirdBooster = Vaccination(doseNumber = 3, totalSerialDoses = 1)
    private val vaccinationOldBooster = Vaccination(doseNumber = 3, totalSerialDoses = 3, product = "EU/1/20/1507")

    @Test
    fun `Vaccination booster test`() {
        assertTrue(vaccinationFirstBooster.isBooster)
        assertTrue(vaccinationSecondBooster.isBooster)
        assertTrue(vaccinationThirdBooster.isBooster)
        assertTrue(vaccinationOldBooster.isBooster)
        assertFalse(vaccinationComplete.isBooster)
        assertFalse(vaccinationIncomplete.isBooster)
    }

    @Test
    fun `Vaccination complete test`() {
        assertTrue(vaccinationFirstBooster.isComplete)
        assertTrue(vaccinationSecondBooster.isComplete)
        assertTrue(vaccinationThirdBooster.isComplete)
        assertTrue(vaccinationOldBooster.isComplete)
        assertTrue(vaccinationComplete.isComplete)
        assertFalse(vaccinationIncomplete.isComplete)
    }

    @Test
    fun `Vaccination complete after recovery test`() {
        assertTrue(vaccinationCompleteAfterRecovery.hasFullProtectionAfterRecovery)
        assertFalse(vaccinationFirstBooster.hasFullProtectionAfterRecovery)
        assertFalse(vaccinationSecondBooster.hasFullProtectionAfterRecovery)
        assertFalse(vaccinationThirdBooster.hasFullProtectionAfterRecovery)
        assertFalse(vaccinationOldBooster.hasFullProtectionAfterRecovery)
        assertFalse(vaccinationComplete.hasFullProtectionAfterRecovery)
        assertFalse(vaccinationIncomplete.hasFullProtectionAfterRecovery)
    }

    @Test
    fun `Vaccination has full protection`() {
        assertTrue(vaccinationCompleteAfterRecovery.hasFullProtection)
        assertTrue(vaccinationFirstBooster.hasFullProtection)
        assertTrue(vaccinationCompleteWithFullProtection.hasFullProtection)
        assertTrue(vaccinationSecondBooster.hasFullProtection)
        assertTrue(vaccinationThirdBooster.hasFullProtection)
        assertTrue(vaccinationOldBooster.hasFullProtection)
        assertFalse(vaccinationComplete.hasFullProtection)
        assertFalse(vaccinationIncomplete.hasFullProtection)
    }

    @Test
    fun `Vaccination type validation`() {
        assertEquals(VaccinationCertType.VACCINATION_FULL_PROTECTION, vaccinationCompleteAfterRecovery.type)
        assertEquals(VaccinationCertType.VACCINATION_FULL_PROTECTION, vaccinationFirstBooster.type)
        assertEquals(VaccinationCertType.VACCINATION_FULL_PROTECTION, vaccinationCompleteWithFullProtection.type)
        assertEquals(VaccinationCertType.VACCINATION_FULL_PROTECTION, vaccinationSecondBooster.type)
        assertEquals(VaccinationCertType.VACCINATION_FULL_PROTECTION, vaccinationThirdBooster.type)
        assertEquals(VaccinationCertType.VACCINATION_FULL_PROTECTION, vaccinationOldBooster.type)
        assertEquals(VaccinationCertType.VACCINATION_COMPLETE, vaccinationComplete.type)
        assertEquals(VaccinationCertType.VACCINATION_INCOMPLETE, vaccinationIncomplete.type)
    }
}

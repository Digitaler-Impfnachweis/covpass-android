/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.app.validitycheck.countries.CountryResolver.defaultDeDomesticCountry
import de.rki.covpass.sdk.cert.CovPassRulesValidator
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.isBeforeUpdateInterval
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ValidityCheckViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val euRulesValidator: CovPassRulesValidator = sdkDeps.euRulesValidator,
    private val domesticRulesValidator: CovPassRulesValidator = sdkDeps.domesticRulesValidator,
    private val sdkDependencies: SdkDependencies = sdkDeps,
) : BaseReactiveState<BaseEvents>(scope) {

    val validationResults: MutableValueFlow<List<CertsValidationResults>> = MutableValueFlow(emptyList())
    val country: MutableValueFlow<Country> = MutableValueFlow(defaultDeDomesticCountry)
    val date: MutableValueFlow<LocalDateTime> = MutableValueFlow(LocalDateTime.now())
    val isInvalidCertAvailable: MutableValueFlow<Boolean> = MutableValueFlow(false)

    init {
        launch {
            showInvalidCertsWarning()
            updateRulesAndCountries()
            validateCertificates()
        }
    }

    fun startUpdateRulesAndCountries() {
        launch {
            updateRulesAndCountries()
        }
    }

    private suspend fun updateRulesAndCountries() {
        if (sdkDependencies.rulesUpdateRepository.lastEuRulesUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.covPassEuRulesRepository.loadRules()
        }
        if (sdkDependencies.rulesUpdateRepository.lastDomesticRulesUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.covPassDomesticRulesRepository.loadRules()
        }
        if (sdkDependencies.rulesUpdateRepository.lastCountryListUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.covPassCountriesRepository.loadCountries()
        }
    }

    private fun showInvalidCertsWarning() {
        val groupedCertList = certRepository.certs.value
        isInvalidCertAvailable.value =
            groupedCertList.certificates.size > groupedCertList.getValidCertificates().size
    }

    private suspend fun validateCertificates() {
        validationResults.value = certRepository.certs.value.getValidCertificates().map {
            val covCertificate = it.getMainCertificate().covCertificate
            CertsValidationResults(
                covCertificate,
                if (country.value.countryCode.equals(defaultDeDomesticCountry.countryCode, ignoreCase = true)) {
                    domesticRulesValidator.validate(
                        cert = covCertificate,
                        validationClock = ZonedDateTime.of(date.value, ZoneId.systemDefault())
                    )
                } else {
                    euRulesValidator.validate(
                        covCertificate,
                        country.value.countryCode.lowercase(),
                        ZonedDateTime.of(date.value, ZoneId.systemDefault())
                    )
                }
            )
        }
    }

    fun updateCountry(updatedCountry: Country) {
        launch {
            country.value = updatedCountry
            validateCertificates()
        }
    }

    fun updateDate(updatedDate: LocalDateTime) {
        launch {
            date.value = updatedDate
            validateCertificates()
        }
    }
}

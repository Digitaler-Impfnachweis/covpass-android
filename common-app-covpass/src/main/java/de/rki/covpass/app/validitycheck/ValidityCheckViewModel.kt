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
import de.rki.covpass.app.validitycheck.countries.CountryRepository.defaultCountry
import de.rki.covpass.sdk.cert.RulesValidator
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.CovPassRulesRepository
import de.rki.covpass.sdk.rules.CovPassValueSetsRepository
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ValidityCheckViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val rulesValidator: RulesValidator = sdkDeps.rulesValidator,
    private val rulesRepository: CovPassRulesRepository = sdkDeps.covPassRulesRepository,
    private val valueSetsRepository: CovPassValueSetsRepository = sdkDeps.covPassValueSetsRepository,
) : BaseReactiveState<BaseEvents>(scope) {

    val validationResults: MutableValueFlow<List<CertsValidationResults>> = MutableValueFlow(emptyList())
    val country: MutableValueFlow<Country> = MutableValueFlow(defaultCountry)
    val date: MutableValueFlow<LocalDateTime> = MutableValueFlow(LocalDateTime.now())

    init {
        loadRulesAndValidateCertificates()
    }

    fun loadRulesAndValidateCertificates() {
        launch {
            loadRulesAndValueSets()
            validateCertificates()
        }
    }

    private suspend fun loadRulesAndValueSets() {
        rulesRepository.loadRules()
        valueSetsRepository.loadValueSets()
    }

    private suspend fun validateCertificates() {
        validationResults.value = certRepository.certs.value.certificates.map {
            val covCertificate = it.getMainCertificate().covCertificate
            CertsValidationResults(
                covCertificate,
                rulesValidator.validate(
                    covCertificate,
                    country.value.countryCode.lowercase(),
                    ZonedDateTime.of(date.value, ZoneId.systemDefault())
                )
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

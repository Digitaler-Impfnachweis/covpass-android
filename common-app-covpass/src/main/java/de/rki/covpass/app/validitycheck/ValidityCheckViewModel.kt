/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.app.validitycheck.countries.CountryRepository.defaultCountry
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.RulesValidator
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.DefaultCovPassRulesRepository
import de.rki.covpass.sdk.rules.DefaultCovPassValueSetsRepository
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.sdk.utils.ExperimentalHCertApi
import kotlinx.coroutines.CoroutineScope
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class ValidityCheckViewModel(
    scope: CoroutineScope,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val rulesValidator: RulesValidator = sdkDeps.rulesValidator,
    private val rulesRepository: DefaultCovPassRulesRepository = sdkDeps.rulesRepository,
    private val valueSetsRepository: DefaultCovPassValueSetsRepository = sdkDeps.valueSetsRepository
) : BaseReactiveState<BaseEvents>(scope) {

    val validationResults: MutableValueFlow<List<CertsValidationResults>> = MutableValueFlow(emptyList())
    val isCertListEmpty: MutableValueFlow<Boolean> = MutableValueFlow(false)
    val country: MutableValueFlow<Country> = MutableValueFlow(defaultCountry)
    val date: MutableValueFlow<LocalDateTime> = MutableValueFlow(LocalDateTime.now())

    init {
        checkCertListSize()
    }

    private fun checkCertListSize() {
        isCertListEmpty.value = certRepository.certs.value.certificates.size == 0
    }

    @ExperimentalHCertApi
    fun loadRulesAndValueSets() {
        launch {
            try {
                rulesRepository.loadRules()
                valueSetsRepository.loadValueSets()
            } catch (e: UnknownHostException) {
                Lumber.e(e)
            } catch (e: SocketTimeoutException) {
                Lumber.e(e)
            }
        }
    }

    fun validateCertificates() {
        launch {
            validationResults.value = certRepository.certs.value.certificates.map {
                val covCertificate = it.getMainCertificate().covCertificate
                CertsValidationResults(
                    covCertificate,
                    rulesValidator.validate(
                        covCertificate,
                        country.value.countryCode.lowercase(),
                        date.value.atZone(ZoneOffset.UTC)
                    )
                )
            }
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

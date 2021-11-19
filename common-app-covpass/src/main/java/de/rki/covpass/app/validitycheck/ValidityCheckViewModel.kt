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
import de.rki.covpass.app.validitycheck.countries.CountryResolver.defaultCountry
import de.rki.covpass.sdk.cert.RulesValidator
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ValidityCheckViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val rulesValidator: RulesValidator = sdkDeps.rulesValidator,
) : BaseReactiveState<BaseEvents>(scope) {

    val validationResults: MutableValueFlow<List<CertsValidationResults>> = MutableValueFlow(emptyList())
    val country: MutableValueFlow<Country> = MutableValueFlow(defaultCountry)
    val date: MutableValueFlow<LocalDateTime> = MutableValueFlow(LocalDateTime.now())
    val isInvalidCertAvailable: MutableValueFlow<Boolean> = MutableValueFlow(false)

    init {
        launch {
            showInvalidCertsWarning()
            validateCertificates()
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

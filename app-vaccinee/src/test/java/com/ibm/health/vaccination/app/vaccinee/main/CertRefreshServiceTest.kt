package com.ibm.health.vaccination.app.vaccinee.main

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ensody.reactivestate.test.CoroutineTest
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
import com.ibm.health.vaccination.sdk.android.cert.CertService
import com.ibm.health.vaccination.sdk.android.cert.models.ExtendedVaccinationCertificate
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificate
import io.ktor.client.features.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test
import java.net.UnknownHostException

internal class CertRefreshServiceTest : CoroutineTest() {

    private val certs = SuspendMutableValueFlow(MutableValueFlow(GroupedCertificatesList())) {}
    private val certService: CertService = mockk()
    private val refreshService by lazy {
        CertRefreshService(coroutineTestRule.testCoroutineScope, certService, certs)
    }

    private val certId = "certid"
    private val vaccinationQrContent = "vacc"
    private val validationQrContent = "vali"

    @Test
    fun `refreshing in background`() = runBlockingTest {
        coEvery { certService.getValidationCert(vaccinationQrContent) } throws UnknownHostException()

        // As long we get errors we don't refresh
        addCert()
        advanceTimeBy(1000 * 20)
        assertThat(certs.value.certificates.first().incompleteCertificate?.validationQrContent).isNull()

        // Due to exponential backoff we only notice the service becoming available again with a delay
        coEvery { certService.getValidationCert(vaccinationQrContent) } returns validationQrContent
        advanceTimeBy(1000 * 8)
        assertThat(certs.value.certificates.first().incompleteCertificate?.validationQrContent).isNull()

        // Now we notice it
        advanceTimeBy(1000 * 8)
        assertThat(certs.value.certificates.first().incompleteCertificate?.validationQrContent)
            .isEqualTo(validationQrContent)
    }

    @Test
    fun `trigger loop with UnknownHostException`() = runBlockingTest {
        coEvery { certService.getValidationCert(vaccinationQrContent) } throws UnknownHostException()

        // For UnknownHostException we want to refresh with exponential backoff

        // Do two requests here
        addCert()
        advanceTimeBy(1500)

        // Only do one request here
        refreshService.triggerUpdate()
        advanceTimeBy(500)

        // Do four requests here
        refreshService.triggerUpdate()
        advanceTimeBy(500 + 1000 + 2000 + 4000)

        // Do one last request to finish the loop
        coEvery { certService.getValidationCert(vaccinationQrContent) } returns validationQrContent
        refreshService.triggerUpdate()
        advanceTimeBy(500)

        coVerify(exactly = 8) { certService.getValidationCert(any()) }
    }

    @Test
    fun `trigger loop with ClientRequestException`() = runBlockingTest {
        coEvery { certService.getValidationCert(vaccinationQrContent) } throws mockk<ClientRequestException>()

        // As long we get errors we don't refresh

        // Only do one request here
        addCert()
        advanceTimeBy(1000)

        // Do no more request here
        refreshService.triggerUpdate()
        advanceTimeBy(1000 * 60 * 60)

        coVerify(exactly = 1) { certService.getValidationCert(any()) }
        assertThat(refreshService.failingCertIds.value.first()).isEqualTo(certId)
    }

    private suspend fun addCert() {
        refreshService

        certs.update {
            it.addCertificate(
                ExtendedVaccinationCertificate(
                    VaccinationCertificate(id = certId),
                    vaccinationQrContent,
                    null,
                )
            )
        }
    }
}

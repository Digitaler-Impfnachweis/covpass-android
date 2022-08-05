/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.DetailExportPdfBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.pdfexport.BaseExportPdfFragment
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import kotlinx.parcelize.Parcelize

@Parcelize
internal class DetailExportPdfFragmentNav(var certId: String) : FragmentNav(DetailExportPdfFragment::class)

internal class DetailExportPdfFragment : BaseExportPdfFragment() {

    override val buttonTextRes = R.string.certificate_create_pdf_list_button
    override val announcementAccessibilityRes: Int =
        R.string.accessibility_vaccination_certificate_detail_view_pdf_announce
    override val viewModel by reactiveState { DetailExportPdfViewModel(scope) }
    override val shareIntentTitle: Int = R.string.certificate_share_pdf_title_android
    override val webView by lazy { binding.webview }

    private val args: DetailExportPdfFragmentNav by lazy { getArgs() }
    private val binding by viewBinding(DetailExportPdfBinding::inflate)
    private val certs by lazy { covpassDeps.certRepository.certs.value }
    private val bulletPoints: List<Int> by lazy {
        listOf(
            R.string.certificate_create_pdf_first_list_item,
            R.string.certificate_create_pdf_second_list_item,
            R.string.certificate_create_pdf_third_list_item,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.certificate_create_pdf_headline)
        fillContent()
    }

    private fun fillContent() {
        bulletPoints.forEach {
            val paragraphItem = layoutInflater.inflate(
                R.layout.onboarding_consent_paragraph_item,
                binding.exportPdfInfoItemsContainer,
                false,
            ).apply { findViewById<TextView>(R.id.content).text = getString(it) }
            binding.exportPdfInfoItemsContainer.addView(paragraphItem)
        }
    }

    override fun onActionButtonClicked() {
        val cert = certs.getCombinedCertificate(args.certId) ?: throw NullCertificateException()
        viewModel.onShareClick(cert)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.exportPdfInfoItemsContainer.isGone = isLoading
        bottomSheetBinding.bottomSheetActionButton.isGone = isLoading
        binding.loadingLayout.isVisible = isLoading
    }

    /** This exception is thrown when no [CombinedCovCertificate] with the given id is found. */
    class NullCertificateException : IllegalStateException()
}

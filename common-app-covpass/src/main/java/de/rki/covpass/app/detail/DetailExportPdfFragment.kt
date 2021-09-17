/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.DetailExportPdfBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import kotlinx.parcelize.Parcelize

@Parcelize
internal class DetailExportPdfFragmentNav(var certId: String) : FragmentNav(DetailExportPdfFragment::class)

internal class DetailExportPdfFragment : BaseBottomSheet(), SharePdfEvents {

    override val buttonTextRes = R.string.certificate_create_pdf_list_button

    private val args: DetailExportPdfFragmentNav by lazy { getArgs() }
    private val certs by lazy { covpassDeps.certRepository.certs.value }
    private val bulletPoints: List<Int> by lazy {
        listOf(
            R.string.certificate_create_pdf_first_list_item,
            R.string.certificate_create_pdf_second_list_item,
            R.string.certificate_create_pdf_third_list_item
        )
    }

    private val detailExportPdfViewModel by reactiveState {
        DetailExportPdfViewModel(scope)
    }
    private val binding by viewBinding(DetailExportPdfBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.certificate_create_pdf_headline)
        fillContent()
        autoRun { showLoading(get(loading) > 0) }
        autoRun { loadDataToWebView(get(detailExportPdfViewModel.pdfString)) }
    }

    override fun onSharePdf(uri: Uri) {
        sharePdf(uri)
        findNavigator().pop()
    }

    private fun initWebView() {
        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.let {
                    detailExportPdfViewModel.onShareStart(
                        it.createPrintDocumentAdapter("Certificate PDF")
                    )
                }
            }
        }
    }

    private fun fillContent() {
        bulletPoints.forEach {
            val paragraphItem = layoutInflater.inflate(
                R.layout.onboarding_consent_paragraph_item,
                binding.exportPdfInfoItemsContainer,
                false
            ).apply { findViewById<TextView>(R.id.content).text = getString(it) }
            binding.exportPdfInfoItemsContainer.addView(paragraphItem)
        }
    }

    private fun sharePdf(uri: Uri) {
        val intent = ShareCompat.IntentBuilder(requireContext())
            .setType("application/pdf")
            .setStream(uri)
            .intent
        val savePdfIntent = Intent(requireContext(), DetailExportPdfSaveOptionActivity::class.java)
        savePdfIntent.data = uri

        val shareIntent = Intent.createChooser(intent, getString(R.string.certificate_share_pdf_title_android))
        shareIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(savePdfIntent))
        startActivity(shareIntent)
    }

    override fun onActionButtonClicked() {
        val cert = certs.getCombinedCertificate(args.certId) ?: throw NullCertificateException()
        detailExportPdfViewModel.onShareClick(cert)
    }

    private fun loadDataToWebView(pdfString: String) {
        if (pdfString.isNotEmpty()) {
            binding.webview.loadDataWithBaseURL(
                null,
                pdfString,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.exportPdfInfoItemsContainer.isGone = isLoading
        bottomSheetBinding.bottomSheetActionButton.isGone = isLoading
        binding.loadingLayout.isVisible = isLoading
    }

    /** This exception is thrown when no [CombinedCovCertificate] with the given id is found. */
    class NullCertificateException : IllegalStateException()
}

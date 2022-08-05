/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.pdfexport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ShareCompat
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseBottomSheet

public abstract class BaseExportPdfFragment : BaseBottomSheet(), SharePdfEvents {

    public abstract val viewModel: BaseExportPdfViewModel
    public abstract val shareIntentTitle: Int
    public abstract val webView: WebView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        autoRun { showLoading(get(loading) > 0) }
        autoRun { loadDataToWebView(get(viewModel.pdfString)) }
    }

    override fun onSharePdf(uri: Uri) {
        sharePdf(uri)
        findNavigator().pop()
    }

    private fun initWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.let {
                    viewModel.onShareStart(
                        it.createPrintDocumentAdapter("Certificate PDF"),
                    )
                }
                viewModel.pdfString.value = ""
            }
        }
    }

    protected fun sharePdf(uri: Uri) {
        val intent = ShareCompat.IntentBuilder(requireContext())
            .setType("application/pdf")
            .setStream(uri)
            .intent
        val savePdfIntent = Intent(requireContext(), ExportPdfSaveOptionActivity::class.java)
        savePdfIntent.data = uri

        val shareIntent = Intent.createChooser(intent, getString(shareIntentTitle))
        shareIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(savePdfIntent))
        startActivity(shareIntent)
    }

    protected fun loadDataToWebView(pdfString: String) {
        if (pdfString.isNotEmpty()) {
            webView.loadDataWithBaseURL(
                null,
                pdfString,
                "text/html",
                "UTF-8",
                null,
            )
        }
    }

    public abstract fun showLoading(isLoading: Boolean)
}

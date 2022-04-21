/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.revocation

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.RevocationExportPdfBinding
import de.rki.covpass.commonapp.pdfexport.BaseExportPdfFragment
import de.rki.covpass.sdk.cert.models.ExpertModeData
import kotlinx.parcelize.Parcelize

@Parcelize
internal class RevocationExportFragmentNav(
    val revocationExportData: ExpertModeData,
    val isGermanCertificate: Boolean
) : FragmentNav(RevocationExportFragment::class)

internal class RevocationExportFragment : BaseExportPdfFragment() {

    override val buttonTextRes = R.string.revocation_detail_page_button_text
    override val announcementAccessibilityRes = R.string.accessibility_revocation_detail_page_announce
    override val viewModel by reactiveState { RevocationExportViewModel(scope) }
    override val shareIntentTitle = R.string.revocation_detail_page_title
    override val webView by lazy { binding.webview }
    private val binding by viewBinding(RevocationExportPdfBinding::inflate)
    private val args: RevocationExportFragmentNav by lazy { getArgs() }
    private val revocationExportData by lazy { args.revocationExportData }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.revocation_detail_page_title)
        bottomSheetBinding.bottomSheetActionButton.isEnabled = args.isGermanCertificate
        fillContent()
    }

    private fun fillContent() {
        val list = mutableListOf(
            RevocationExportDetailItem(
                R.string.revocation_detail_page_transaction_number,
                revocationExportData.transactionNumber
            ),
            RevocationExportDetailItem(
                R.string.revocation_detail_page_key_reference,
                revocationExportData.kid
            ),
            RevocationExportDetailItem(
                R.string.revocation_detail_page_country,
                revocationExportData.issuingCountry
            ),
            RevocationExportDetailItem(
                R.string.revocation_detail_page_technical_expiry_date,
                revocationExportData.technicalExpiryDate
            ),
            RevocationExportDetailItem(
                R.string.revocation_detail_page_date_of_issuance,
                revocationExportData.dateOfIssue
            )
        )
        RevocationExportDetailAdapter(this).apply {
            updateList(list)
            attachTo(binding.revocationExportPdfRecyclerview)
        }
    }

    override fun onSharePdf(uri: Uri) {
        sharePdf(uri)
    }

    override fun onActionButtonClicked() {
        viewModel.onShareClick(args.revocationExportData)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.revocationExportPdfRecyclerview.isGone = isLoading
        bottomSheetBinding.bottomSheetActionButton.isGone = isLoading
        binding.loadingLayout.isVisible = isLoading
    }
}

/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing.result

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ResultBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.validityresult.DerivedValidationResult
import de.rki.covpass.app.validityresult.LocalResult
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.ticketing.data.validate.BookingPortalValidationResponseResult
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse

public abstract class TicketingResultFragment : BaseBottomSheet() {

    private val binding by viewBinding(ResultBinding::inflate)

    override val heightLayoutParams: Int by lazy { ViewGroup.LayoutParams.MATCH_PARENT }
    private var titleString: String? = null
    private lateinit var resultType: LocalResult
    private val certs by lazy { covpassDeps.certRepository.certs }
    public val derivedValidationResults: MutableList<DerivedValidationResult> = mutableListOf()
    protected abstract val certId: String
    protected abstract val subtitleString: String
    protected abstract val subtitleAccessibleDescription: String
    protected abstract val bookingValidationResponse: BookingValidationResponse
    protected abstract val ticketingDataInitialization: TicketingDataInitialization
    protected abstract val resultNoteEn: Int
    protected abstract val resultNoteDe: Int
    protected abstract val validationServiceId: String
    override val buttonTextRes: Int = R.string.accessibility_share_certificate_transmission_label_close

    public abstract fun getRowList(cert: CovCertificate): List<TicketingResultRowData>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleString = certs.value.getCombinedCertificate(certId)?.covCertificate?.fullName
        resultType = when (bookingValidationResponse.result) {
            BookingPortalValidationResponseResult.OK -> LocalResult.PASSED
            BookingPortalValidationResponseResult.NOK -> LocalResult.FAIL
            BookingPortalValidationResponseResult.CHK -> LocalResult.OPEN
        }

        startRecyclerView()
        bottomSheetBinding.bottomSheetTitle.text = titleString
        bottomSheetBinding.bottomSheetSubtitle.text = subtitleString
        bottomSheetBinding.bottomSheetSubtitle.contentDescription = subtitleAccessibleDescription
        bottomSheetBinding.bottomSheetSubtitle.isVisible = true
        bottomSheetBinding.bottomSheetActionButton.isVisible = true
    }

    private fun startRecyclerView() {
        val cert = certs.value.getCombinedCertificate(certId)?.covCertificate ?: return
        TicketingResultAdapter(this, resultNoteEn, resultNoteDe).apply {
            updateCert(certId)
            updateHeaderWarning(resultType, ticketingDataInitialization, validationServiceId)
            updateList(getRowList(cert).filterNot { it.value.isNullOrEmpty() })
            attachTo(binding.resultRecyclerView)
        }
    }

    override fun onActionButtonClicked() {
        findNavigator().popAll()
    }

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onClickOutside() {
        onCloseButtonClicked()
    }

    public data class TicketingResultRowData(
        val title: String,
        val titleAccessibleDescription: String,
        val value: String?,
        val validationResult: List<DerivedValidationResult> = emptyList(),
        val description: String? = null,
        val valueAccessibleDescription: String? = null,
    )
}

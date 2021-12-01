/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateFilteringTicketingBinding
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.ticketing.BookingPortalEncryptionData
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.ticketing.TicketingType
import kotlinx.parcelize.Parcelize

public interface FilterClickListener {
    public fun onCovCertificateClicked(
        certId: String,
        qrString: String,
        encryptionData: BookingPortalEncryptionData,
    )
}

@Parcelize
public class CertificateFilteringTicketingFragmentNav(
    public val ticketingDataInitialization: TicketingDataInitialization,
) : FragmentNav(CertificateFilteringTicketingFragment::class)

public class CertificateFilteringTicketingFragment :
    BaseTicketingFragment(),
    CertificateFilteringEvents,
    FilterClickListener {

    public val args: CertificateFilteringTicketingFragmentNav by lazy { getArgs() }

    private val binding by viewBinding(CertificateFilteringTicketingBinding::inflate)

    @Suppress("UnusedPrivateMember")
    private val viewModel by reactiveState {
        CertificateFilteringTicketingViewModel(
            scope = scope,
            ticketingDataInitialization = args.ticketingDataInitialization
        )
    }
    private lateinit var adapter: CertificateFilteringTicketingAdapter

    override val heightLayoutParams: Int = ViewGroup.LayoutParams.MATCH_PARENT
    override val buttonTextRes: Int = R.string.share_certificate_selection_no_match_action_button
    override val cancelProcess: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.share_certificate_selection_title)
        binding.certificateFilteringHeader.setText(R.string.share_certificate_selection_message)
        bottomSheetBinding.bottomSheetActionButton.isVisible = false
        adapter = CertificateFilteringTicketingAdapter(this, this)
        adapter.attachTo(binding.certificateFilteringCertificatesRecycler)
        bottomSheetBinding.bottomSheet.setOnClickListener(null)
    }

    override fun onActionButtonClicked() {
        onCloseButtonClicked()
    }

    override fun setLoading(isLoading: Boolean) {
        binding.certificateFilteringLoadingLayout.isVisible = isLoading
    }

    override fun onFilteringCompleted(
        list: List<CombinedCovCertificate>,
        encryptionData: BookingPortalEncryptionData,
    ) {
        binding.certificateFilteringCertificatesRecycler.isVisible = true
        adapter.updateList(list, encryptionData)
    }

    override fun showUserData(firstName: String, lastName: String, dob: String, types: List<TicketingType>) {
        var data = "$firstName $lastName\n"
        data += "${getString(R.string.share_certificate_selection_requirements_date_of_birth)}: $dob\n"
        types.forEachIndexed { index, ticketingType ->
            data += when (ticketingType) {
                TicketingType.Vaccination -> getString(R.string.certificates_overview_vaccination_certificate_title)
                TicketingType.Recovery -> getString(R.string.certificates_overview_recovery_certificate_title)
                TicketingType.Test.Antigen,
                TicketingType.Test.Generic,
                TicketingType.Test.Pcr,
                -> getString(R.string.certificates_overview_test_certificate_title)
            }
            if (index != types.size - 1) {
                data += ", "
            }
        }
        binding.certificateFilteringData.text = data
        binding.certificateFilteringData.isVisible = true
    }

    override fun onEmptyList() {
        binding.certificateFilteringCertificatesRecycler.isGone = true
        binding.certificateFilteringEmptyCertificatesLayout.isVisible = true
        bottomSheetBinding.bottomSheetActionButton.isVisible = true
    }

    override fun onCovCertificateClicked(
        certId: String,
        qrString: String,
        encryptionData: BookingPortalEncryptionData,
    ) {
        val validationObject = ValidationTicketingTestObject(
            qrString,
            encryptionData.keyPair,
            encryptionData.accessTokenContainer.ticketingAccessTokenData.jwtToken,
            encryptionData.ticketingValidationServiceIdentity,
            encryptionData.accessTokenContainer.ticketingAccessTokenData.iv,
            encryptionData.accessTokenContainer.accessToken.validationUrl,
            encryptionData.validationServiceId
        )
        findNavigator().push(
            ConsentSendTicketingFragmentNav(
                certId,
                args.ticketingDataInitialization,
                validationObject
            )
        )
    }
}

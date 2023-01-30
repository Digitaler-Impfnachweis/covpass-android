/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.misuseprevention

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isGone
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.MisusePreventionBinding
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.utils.isLandscapeMode
import de.rki.covpass.commonapp.utils.stripUnderlinesAndSetExternalLinkImage
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MisusePreventionFragmentNav(
    val qrContent: String,
) : FragmentNav(MisusePreventionFragment::class)

internal class MisusePreventionFragment : BaseBottomSheet(), MisusePreventionEvents {

    private val binding by viewBinding(MisusePreventionBinding::inflate)
    private val viewModel: MisusePreventionViewModel by reactiveState { MisusePreventionViewModel(scope) }

    val args: MisusePreventionFragmentNav by lazy { getArgs() }
    override val buttonTextRes = R.string.certificate_add_warning_maximum_button_primary

    override val announcementAccessibilityRes: Int = R.string.accessibility_certificate_add_warning_maximum_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.misusePreventionContent.apply {
            text = getSpanned(R.string.certificate_add_warning_maximum_copy_android)
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlinesAndSetExternalLinkImage()
        }
        bottomSheetBinding.bottomSheetHeader.isGone = true
        bottomSheetBinding.bottomSheetClose.isGone = true
        bottomSheetBinding.bottomSheetBottomView.isGone = true

        binding.misusePreventionIllustration.isGone = resources.isLandscapeMode()
        binding.misusePreventionActionButton.setOnClickListener {
            viewModel.addNewCertificate(args.qrContent)
        }
        binding.misusePreventionCancelButton.setOnClickListener {
            onCloseButtonClicked()
        }
    }

    override fun onSaveSuccess(groupedCertificatesId: GroupedCertificatesId, certId: String) {
        findNavigator().popAll()
        findNavigator().push(
            DetailFragmentNav(
                groupedCertificatesId,
                certId = certId,
                isFirstAdded = true,
            ),
        )
    }

    override fun onActionButtonClicked() {}
}

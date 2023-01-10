/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ReissueNotificationPopupContentBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.commonapp.utils.isLandscapeMode
import de.rki.covpass.sdk.cert.models.ReissueType
import kotlinx.parcelize.Parcelize

@Parcelize
public class ReissueNotificationFragmentNav(
    public val reissueType: ReissueType,
    public val listCertIds: List<String>,
) : FragmentNav(ReissueNotificationFragment::class)

public class ReissueNotificationFragment :
    ReissueBaseFragment(),
    ReissueNotificationEvents {

    private val binding by viewBinding(ReissueNotificationPopupContentBinding::inflate)
    private val viewModel by reactiveState { ReissueNotificationViewModel(scope, args.listCertIds) }
    private val args: ReissueNotificationFragmentNav by lazy { getArgs() }
    override val buttonTextRes: Int = R.string.certificate_renewal_startpage_main_button
    override val announcementAccessibilityRes: Int =
        R.string.accessibility_popup_renew_certificate_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_popup_renew_certificate_closing_announce

    private val combinedCovCertificate by lazy {
        covpassDeps.certRepository.certs.value.getCombinedCertificate(args.listCertIds[0])
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetClose.isVisible = false
        bottomSheetBinding.bottomSheetTextButton.apply {
            setText(R.string.certificate_renewal_startpage_secondary_button)
            isVisible = true
            setOnClickListener {
                updateHasSeenReissueNotification(false)
            }
        }
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
        bottomSheetBinding.bottomSheetTitle.setText(
            if (args.reissueType == ReissueType.Booster) {
                R.string.certificate_renewal_startpage_headline
            } else {
                R.string.renewal_expiry_notification_title
            },
        )

        ReissueContentAdapter(this).apply {
            combinedCovCertificate?.let {
                it.toReissueCertificateItem(true)?.let { certificate ->
                    updateList(listOf(certificate))
                }
            }
            attachTo(binding.reissueNotificationCertificateList)
        }
        binding.reissueNotificationImage.isGone = resources.isLandscapeMode()
        binding.reissueNotificationNote.setText(
            when (args.reissueType) {
                ReissueType.Booster -> R.string.certificate_renewal_startpage_copy
                ReissueType.Recovery -> R.string.renewal_expiry_notification_copy_recovery
                ReissueType.Vaccination -> R.string.renewal_expiry_notification_copy_vaccination
                ReissueType.None -> R.string.certificate_renewal_startpage_copy
            },
        )
        binding.reissueNotificationInfoElement.showInfo(
            getString(R.string.certificate_renewal_startpage_copy_box),
            titleStyle = R.style.DefaultText_OnBackground,
            iconRes = R.drawable.info_icon,
        )
    }

    override fun onClickOutside() {}

    override fun onActionButtonClicked() {
        updateHasSeenReissueNotification()
    }

    override fun onBackPressed(): Abortable {
        updateHasSeenReissueNotification(false)
        return Abort
    }

    private fun updateHasSeenReissueNotification(continueReissue: Boolean = true) {
        viewModel.updateHasSeenReissueNotification(args.reissueType, continueReissue)
    }

    override fun onUpdateHasSeenReissueNotificationFinish(continueReissue: Boolean) {
        if (continueReissue) {
            findNavigator().push(ReissueConsentFragmentNav(args.listCertIds, args.reissueType))
        } else {
            findNavigator().popUntil<ReissueCallback>()?.onReissueCancel()
        }
    }
}

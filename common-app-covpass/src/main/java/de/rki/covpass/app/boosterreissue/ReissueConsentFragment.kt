/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ReissueConsentPopupContentBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.uielements.InfoElementListAdapter
import de.rki.covpass.app.uielements.setValues
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.onboarding.CommonDataProtectionFragmentNav
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.ReissueType
import kotlinx.parcelize.Parcelize

@Parcelize
public class ReissueConsentFragmentNav(
    public val listCertIds: List<String>,
    public val reissueType: ReissueType
) : FragmentNav(ReissueConsentFragment::class)

public class ReissueConsentFragment : ReissueBaseFragment(), DialogListener {

    private val binding by viewBinding(ReissueConsentPopupContentBinding::inflate)
    private val args: ReissueConsentFragmentNav by lazy { getArgs() }
    private val certificateList: List<CombinedCovCertificate> by lazy {
        args.listCertIds.mapNotNull {
            covpassDeps.certRepository.certs.value.getCombinedCertificate(it)
        }
    }
    override val buttonTextRes: Int =
        R.string.certificate_renewal_consent_page_transfer_certificates_confirmation_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetClose.isVisible = false
        bottomSheetBinding.bottomSheetCancelButton.apply {
            setText(R.string.certificate_renewal_consent_page_transfer_certificates_cancel_button)
            isVisible = true
            setOnClickListener {
                showDialogCancelProcess()
            }
        }
        bottomSheetBinding.bottomSheetTitle.setText(
            R.string.certificate_renewal_consent_page_transfer_certificates_headline
        )

        binding.reissueConsentCertificateListTitle.setText(
            R.string.certificate_renewal_consent_page_transfer_certificates_subline
        )
        ReissueContentAdapter(this).apply {
            updateList(certificateList)
            attachTo(binding.reissueConsentCertificateList)
        }
        binding.reissueConsentInfoElement.setValues(
            getString(R.string.certificate_renewal_consent_page_transfer_certificates_consent_box_subline),
            getString(R.string.certificate_renewal_consent_page_transfer_certificates_consent_box_copy),
            iconRes = R.drawable.info_icon,
            backgroundRes = R.drawable.info_background,
            list = if (args.reissueType == ReissueType.Booster) {
                listOf(
                    getString(
                        R.string.certificate_renewal_consent_page_transfer_certificates_consent_box_copy_list_item_1
                    ),
                    getString(
                        R.string.certificate_renewal_consent_page_transfer_certificates_consent_box_copy_list_item_2
                    ),
                    getString(
                        R.string.certificate_renewal_consent_page_transfer_certificates_consent_box_copy_list_item_3
                    )
                )
            } else {
                listOf(
                    getString(R.string.renewal_expiry_consent_box_item_1),
                    getString(R.string.renewal_expiry_consent_box_item_2)
                )
            },
            parent = this
        )
        if (args.reissueType == ReissueType.Booster) {
            binding.reissueConsentInfoElementList.isVisible = false
            binding.reissueConsentUpdateTitle.setText(
                R.string.certificate_renewal_consent_page_transfer_certificates_copy
            )
            binding.reissueConsentUpdateSubtitle.setText(
                R.string.certificate_renewal_consent_page_transfer_certificates_headline_privacy_policy
            )
        } else {
            binding.reissueConsentUpdateTitle.isVisible = false
            binding.reissueConsentUpdateSubtitle.isVisible = false
            binding.reissueConsentInfoElementList.isVisible = true
            InfoElementListAdapter(
                listOf(
                    getString(R.string.renewal_expiry_consent_list_item_1),
                    getString(R.string.renewal_expiry_consent_list_item_2),
                    getString(R.string.renewal_expiry_consent_list_item_3),
                    getString(R.string.renewal_expiry_consent_list_item_4)
                ),
                this
            ).attachTo(binding.reissueConsentInfoElementList)
        }

        binding.reissueConsentUpdateFieldDataPrivacy.apply {
            text = getString(R.string.app_information_title_datenschutz)
            setOnClickListener {
                findNavigator().push(CommonDataProtectionFragmentNav())
            }
        }
    }

    override fun onClickOutside() {}

    override fun onActionButtonClicked() {
        if (args.reissueType == ReissueType.Booster) {
            findNavigator().push(
                ReissueResultFragmentNav(
                    args.listCertIds,
                    args.reissueType
                )
            )
        } else {
            findNavigator().push(
                ReissueExpiredResultFragmentNav(
                    args.listCertIds,
                    args.reissueType
                )
            )
        }
    }

    override fun onBackPressed(): Abortable {
        showDialogCancelProcess()
        return Abort
    }

    private fun showDialogCancelProcess() {
        val dialogModel = DialogModel(
            titleRes = R.string.cancellation_share_certificate_title,
            positiveButtonTextRes = R.string.cancellation_share_certificate_action_button_yes,
            negativeButtonTextRes = R.string.cancellation_share_certificate_action_button_no,
            tag = REISSUE_CONSENT_END_PROCESS,
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == REISSUE_CONSENT_END_PROCESS && action == DialogAction.POSITIVE) {
            findNavigator().popUntil<ReissueCallback>()?.onReissueCancel()
        }
    }

    public companion object {
        public const val REISSUE_CONSENT_END_PROCESS: String = "reissue_consent_end_process"
    }
}

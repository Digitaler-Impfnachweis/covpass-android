/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.navigation.android.triggerBackPress
import de.rki.covpass.app.R
import de.rki.covpass.app.add.AddCovCertificateFragmentNav
import de.rki.covpass.app.databinding.DetailBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.adapter.DetailAdapter
import de.rki.covpass.app.detail.adapter.DetailItem
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.utils.*
import kotlinx.parcelize.Parcelize

/**
 * Interface to communicate events from [DetailFragment] back to other fragments.
 */
internal interface DetailCallback {
    fun onDeletionCompleted()
    fun displayCert(certId: GroupedCertificatesId)
}

public interface DetailClickListener {
    public fun onShowCertificateClicked()
    public fun onNewCertificateScanClicked()
    public fun onCovCertificateClicked(id: String, dgcEntryType: DGCEntryType)
}

@Parcelize
internal class DetailFragmentNav(
    var certId: GroupedCertificatesId,
) : FragmentNav(DetailFragment::class)

/**
 * Fragment which shows the [GroupedCertificates] details
 * Further actions (Show QR Code, Add cov certificate)
 */
internal class DetailFragment : BaseFragment(), DgcEntryDetailCallback, DetailClickListener {

    private val args: DetailFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState { DetailViewModel(scope) }
    private val binding by viewBinding(DetailBinding::inflate)
    private var isFavorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        autoRun { updateViews(get(covpassDeps.certRepository.certs)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val favoriteItem = menu.add(Menu.NONE, FAVORITE_ITEM_ID, Menu.NONE, R.string.certificate_favorite_button_hint)
        val favoriteIcon = if (isFavorite) R.drawable.star_black_fill else R.drawable.star_black
        favoriteItem.setIcon(favoriteIcon)
        favoriteItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == FAVORITE_ITEM_ID) {
            viewModel.onFavoriteClick(args.certId)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onBackPressed(): Abortable {
        findNavigator().popUntil<DetailCallback>()?.displayCert(args.certId)
        return Abort
    }

    override fun onDeletionCompleted(isGroupedCertDeleted: Boolean) {
        if (isGroupedCertDeleted) {
            findNavigator().popUntil<DetailCallback>()?.onDeletionCompleted()
        } else {
            val dialogModel = DialogModel(
                titleRes = R.string.delete_result_dialog_header,
                messageString = getString(R.string.delete_result_dialog_message),
                positiveButtonTextRes = R.string.delete_result_dialog_positive_button_text,
            )
            showDialog(dialogModel, childFragmentManager)
            updateViews(covpassDeps.certRepository.certs.value)
        }
    }

    private fun updateViews(certList: GroupedCertificatesList) {
        val certId = args.certId
        // Can be null after deletion... in this case no update is necessary anymore
        val groupedCertificate = certList.getGroupedCertificates(certId) ?: return
        val mainCertificate = groupedCertificate.getMainCertificate()
        isFavorite = certList.isMarkedAsFavorite(certId)
        setHasOptionsMenu(certList.certificates.size > 1)
        activity?.invalidateOptionsMenu()

        mainCertificate.covCertificate.let { cert ->
            val dgcEntry = cert.dgcEntry
            val isExpiredOrInvalid = when (mainCertificate.status) {
                CertValidationResult.Expired, CertValidationResult.Invalid -> true
                CertValidationResult.ExpiryPeriod, CertValidationResult.Valid -> false
            }
            val certStatus = mainCertificate.status
            val personalDataList = listOf(
                DetailItem.Name(cert.fullName),
                when (dgcEntry) {
                    is Vaccination -> {
                        when (dgcEntry.type) {
                            VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                                val title = getString(
                                    when (certStatus) {
                                        CertValidationResult.Expired -> R.string.certificates_overview_expired_title
                                        CertValidationResult.Invalid -> R.string.certificates_overview_invalid_title
                                        CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                            R.string.vaccination_certificate_overview_complete_title
                                    }
                                )
                                val message = getString(
                                    when (certStatus) {
                                        CertValidationResult.Expired -> R.string.certificates_overview_expired_message
                                        CertValidationResult.Invalid -> R.string.certificates_overview_invalid_message
                                        CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                            R.string.vaccination_certificate_overview_complete_message
                                    }
                                )
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(R.string.vaccination_certificate_overview_complete_action_button_title)
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired,
                                        CertValidationResult.Invalid -> R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                            R.drawable.detail_cert_status_complete
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
                                )
                            }
                            VaccinationCertType.VACCINATION_COMPLETE -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.vaccination_certificate_overview_complete_from_title,
                                            mainCertificate.covCertificate.validDate.formatDateOrEmpty()
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_message)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(R.string.vaccination_certificate_overview_complete_from_message)
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(R.string.vaccination_certificate_overview_complete_action_button_title)
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired, CertValidationResult.Invalid ->
                                            R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                            R.drawable.detail_cert_status_complete
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
                                )
                            }
                            VaccinationCertType.VACCINATION_INCOMPLETE -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.vaccination_certificate_overview_incomplete_title,
                                            dgcEntry.doseNumber,
                                            dgcEntry.totalSerialDoses
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_message)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(R.string.vaccination_certificate_overview_incomplete_message)
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(
                                        R.string.vaccination_certificate_detail_view_incomplete_action_button_title
                                    )
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired, CertValidationResult.Invalid ->
                                            R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                            R.drawable.detail_cert_status_incomplete
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
                                )
                            }
                        }
                    }
                    is Test -> {
                        when (dgcEntry.type) {
                            TestCertType.NEGATIVE_PCR_TEST -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.pcr_test_certificate_overview_title,
                                            dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_message)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(R.string.pcr_test_certificate_overview_message)
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(
                                        R.string.pcr_test_certificate_overview_action_button_title
                                    )
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired, CertValidationResult.Invalid ->
                                            R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                            R.drawable.detail_cert_status_complete
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
                                )
                            }
                            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.test_certificate_overview_title,
                                            dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_message)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                        getString(R.string.test_certificate_overview_message)
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(
                                        R.string.test_certificate_overview_action_button_title
                                    )
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired, CertValidationResult.Invalid ->
                                            R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                            R.drawable.detail_cert_status_complete
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
                                )
                            }
                            TestCertType.POSITIVE_PCR_TEST, TestCertType.POSITIVE_ANTIGEN_TEST -> return
                        }
                    }
                    is Recovery -> {
                        val title = when (certStatus) {
                            CertValidationResult.Expired ->
                                getString(R.string.certificates_overview_expired_title)
                            CertValidationResult.Invalid ->
                                getString(R.string.certificates_overview_invalid_title)
                            CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                if (dgcEntry.validFrom.isInFuture()) {
                                    getString(
                                        R.string.recovery_certificate_overview_valid_from_title,
                                        dgcEntry.validFrom?.formatDateOrEmpty()
                                    )
                                } else {
                                    getString(
                                        R.string.recovery_certificate_overview_valid_until_title,
                                        dgcEntry.validUntil?.formatDateOrEmpty()
                                    )
                                }
                        }
                        val message = when (certStatus) {
                            CertValidationResult.Expired ->
                                getString(R.string.certificates_overview_expired_message)
                            CertValidationResult.Invalid ->
                                getString(R.string.certificates_overview_invalid_message)
                            CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                getString(R.string.recovery_certificate_overview_message)
                        }
                        val buttonText = if (isExpiredOrInvalid) {
                            getString(R.string.certificates_overview_expired_action_button_title)
                        } else {
                            getString(R.string.recovery_certificate_overview_action_button_title)
                        }
                        DetailItem.Widget(
                            title = title,
                            statusIcon = when (certStatus) {
                                CertValidationResult.Expired, CertValidationResult.Invalid ->
                                    R.drawable.detail_cert_status_expired
                                CertValidationResult.Valid, CertValidationResult.ExpiryPeriod ->
                                    R.drawable.detail_cert_status_complete
                            },
                            message = message,
                            buttonText = buttonText,
                            isExpiredOrInvalid = isExpiredOrInvalid
                        )
                    }
                },
                DetailItem.Header(
                    getString(R.string.certificates_overview_personal_data_title)
                ),
                DetailItem.Personal(
                    getString(R.string.certificates_overview_personal_data_name),
                    cert.fullNameReverse
                ),
                DetailItem.Personal(
                    getString(R.string.certificates_overview_personal_data_date_of_birth),
                    cert.birthDateFormatted
                ),
                DetailItem.Header(
                    getString(R.string.certificates_overview_all_certificates_title)
                )
            )

            val sortedCertificatesList = groupedCertificate.getSortedCertificates().mapNotNull {
                when (val groupedDgcEntry = it.covCertificate.dgcEntry) {
                    is Vaccination -> {
                        DetailItem.Certificate(
                            id = groupedDgcEntry.id,
                            type = groupedDgcEntry.type,
                            title = getString(R.string.certificates_overview_vaccination_certificate_title),
                            subtitle = getString(
                                R.string.certificates_overview_vaccination_certificate_message,
                                groupedDgcEntry.doseNumber, groupedDgcEntry.totalSerialDoses
                            ),
                            date = getString(
                                R.string.certificates_overview_vaccination_certificate_date,
                                groupedDgcEntry.occurrence?.formatDate()
                            ),
                            isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                            certStatus = certStatus
                        )
                    }
                    is Test -> {
                        when (groupedDgcEntry.type) {
                            TestCertType.NEGATIVE_PCR_TEST -> {
                                DetailItem.Certificate(
                                    id = groupedDgcEntry.id,
                                    type = groupedDgcEntry.type,
                                    title = getString(R.string.certificates_overview_test_certificate_title),
                                    subtitle = getString(R.string.certificates_overview_pcr_test_certificate_message),
                                    date = getString(
                                        R.string.certificates_overview_pcr_test_certificate_date,
                                        groupedDgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                    ),
                                    isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                                    certStatus = certStatus
                                )
                            }
                            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                                DetailItem.Certificate(
                                    id = groupedDgcEntry.id,
                                    type = groupedDgcEntry.type,
                                    title = getString(R.string.certificates_overview_test_certificate_title),
                                    subtitle = getString(R.string.certificates_overview_test_certificate_message),
                                    date = getString(
                                        R.string.certificates_overview_test_certificate_date,
                                        groupedDgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                    ),
                                    isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                                    certStatus = certStatus
                                )
                            }
                            TestCertType.POSITIVE_PCR_TEST, TestCertType.POSITIVE_ANTIGEN_TEST -> null
                        }
                    }
                    is Recovery -> {
                        val date = if (groupedDgcEntry.validFrom.isInFuture()) {
                            getString(
                                R.string.certificates_overview_recovery_certificate_valid_from_date,
                                groupedDgcEntry.validFrom?.formatDateOrEmpty()
                            )
                        } else {
                            getString(
                                R.string.certificates_overview_recovery_certificate_valid_until_date,
                                groupedDgcEntry.validUntil?.formatDateOrEmpty()
                            )
                        }
                        DetailItem.Certificate(
                            id = groupedDgcEntry.id,
                            type = groupedDgcEntry.type,
                            title = getString(R.string.certificates_overview_recovery_certificate_title),
                            subtitle = getString(R.string.certificates_overview_recovery_certificate_message),
                            date = date,
                            isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                            certStatus = certStatus
                        )
                    }
                }
            }
            DetailAdapter(personalDataList + sortedCertificatesList, this, this)
                .attachTo(binding.detailVaccinationList)
        }
    }

    override fun onShowCertificateClicked() {
        triggerBackPress()
    }

    override fun onNewCertificateScanClicked() {
        findNavigator().push(AddCovCertificateFragmentNav())
    }

    override fun onCovCertificateClicked(id: String, dgcEntryType: DGCEntryType) {
        when (dgcEntryType) {
            VaccinationCertType.VACCINATION_INCOMPLETE,
            VaccinationCertType.VACCINATION_COMPLETE,
            VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                findNavigator().push(VaccinationDetailFragmentNav(id))
            }
            TestCertType.NEGATIVE_PCR_TEST,
            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                findNavigator().push(TestDetailFragmentNav(id))
            }
            RecoveryCertType.RECOVERY -> {
                findNavigator().push(RecoveryDetailFragmentNav(id))
            }
            TestCertType.POSITIVE_PCR_TEST,
            TestCertType.POSITIVE_ANTIGEN_TEST -> return
            // .let{} to enforce exhaustiveness
        }.let {}
    }

    private fun setupActionBar() {
        attachToolbar(binding.detailToolbar)
        val activity = (activity as? AppCompatActivity)
        activity?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                val icon = R.drawable.back_arrow
                setHomeAsUpIndicator(icon)
            }
        }
    }

    private companion object {
        private const val FAVORITE_ITEM_ID = 82957
    }
}

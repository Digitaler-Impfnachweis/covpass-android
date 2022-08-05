/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.importcertificate

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ImportCertificateSelectorPopupContentBinding
import de.rki.covpass.app.errorhandling.ErrorHandler.Companion.TAG_ERROR_MAX_NUMBER_OF_HOLDERS_EXCEEDED
import de.rki.covpass.app.uielements.TripleStateCheckBox.Companion.CHECKED
import de.rki.covpass.app.uielements.TripleStateCheckBox.Companion.INDETERMINATE
import de.rki.covpass.app.uielements.TripleStateCheckBox.Companion.UNCHECKED
import de.rki.covpass.app.uielements.setValues
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.sdk.cert.models.CovCertificate
import kotlinx.parcelize.Parcelize

@Parcelize
public class ImportCertificatesSelectorFragmentNav(
    public val uri: Uri,
    public val fromShareOption: Boolean = false,
) : FragmentNav(ImportCertificatesSelectorFragment::class)

public class ImportCertificatesSelectorFragment : BaseBottomSheet(), ImportCertificatesEvents, DialogListener {

    private val args: ImportCertificatesSelectorFragmentNav by lazy { getArgs() }
    private val uri by lazy { args.uri }
    private val viewModel by reactiveState { ImportCertificatesSelectorViewModel(scope) }
    private val binding by viewBinding(ImportCertificateSelectorPopupContentBinding::inflate)
    private var certificateList: List<ImportCovCertificate> = emptyList()
    private var adapter: ImportCertificateSelectorAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillInfoElement()
        val fileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")
        viewModel.getQrCodes(
            fileDescriptor,
            requireContext().contentResolver.getType(uri)?.contains("pdf") == true,
            resources.displayMetrics,
        )
        binding.checkboxAllCertificates.setState(CHECKED)
        binding.layoutAllCertificateInfo.setOnClickListener {
            when (binding.checkboxAllCertificates.getState()) {
                UNCHECKED -> {
                    binding.checkboxAllCertificates.setState(CHECKED)
                    adapter?.selectAll()
                }
                CHECKED -> {
                    binding.checkboxAllCertificates.setState(UNCHECKED)
                    adapter?.unselectAll()
                }
                INDETERMINATE -> {
                    binding.checkboxAllCertificates.setState(CHECKED)
                    adapter?.selectAll()
                }
            }
            binding.infoAllCertificates.text =
                getString(
                    R.string.file_import_result_bulge_select,
                    adapter?.checkedList?.size,
                    adapter?.itemCount,
                )
            bottomSheetBinding.bottomSheetActionButton.text =
                getString(
                    R.string.file_import_result_button,
                    adapter?.checkedList?.size,
                )
        }
    }

    override fun onActionButtonClicked() {
        if (adapter == null) {
            findNavigator().popAll()
        } else {
            adapter?.getSelectedItems()?.let {
                binding.recyclerViewCertificateList.isVisible = false
                binding.layoutAllCertificateInfo.isVisible = false
                viewModel.addCertificates(it)
            }
        }
    }

    override fun noValidCertificatesFound() {
        binding.recyclerViewCertificateList.isVisible = false
        binding.layoutAllCertificateInfo.isVisible = false
        bottomSheetBinding.bottomSheetTitle.setText(R.string.file_import_result_null_title)
        updateButtonText()
    }

    override fun certificatesFound(list: List<ImportCovCertificate>) {
        binding.recyclerViewCertificateList.isVisible = true
        binding.layoutAllCertificateInfo.isVisible = true
        binding.infoAllCertificates.text =
            getString(
                R.string.file_import_result_bulge_select,
                list.size,
                list.size,
            )
        adapter = ImportCertificateSelectorAdapter(
            this,
            event = {
                val adapter = (binding.recyclerViewCertificateList.adapter as? ImportCertificateSelectorAdapter)
                    ?: return@ImportCertificateSelectorAdapter
                val numCheckedCertificates =
                    adapter.checkedList.size
                when (numCheckedCertificates) {
                    0 -> {
                        bottomSheetBinding.bottomSheetActionButton.isEnabled = false
                        binding.checkboxAllCertificates.setState(UNCHECKED)
                    }
                    adapter.itemCount -> {
                        bottomSheetBinding.bottomSheetActionButton.isEnabled = true
                        binding.checkboxAllCertificates.setState(CHECKED)
                    }
                    else -> {
                        bottomSheetBinding.bottomSheetActionButton.isEnabled = true
                        binding.checkboxAllCertificates.setState(INDETERMINATE)
                    }
                }
                bottomSheetBinding.bottomSheetActionButton.text =
                    getString(
                        R.string.file_import_result_button,
                        numCheckedCertificates,
                    )
                binding.infoAllCertificates.text =
                    getString(
                        R.string.file_import_result_bulge_select,
                        numCheckedCertificates,
                        adapter.itemCount,
                    )
            },
        ).apply {
            updateList(list)
            attachTo(binding.recyclerViewCertificateList)
        }

        bottomSheetBinding.bottomSheetTitle.setText(R.string.file_import_result_title)
        certificateList = list
        updateButtonText()
    }

    override fun errorFailedToOpenFile() {
        updateLoading(true)
    }

    override fun addCertificatesFinish() {
        findNavigator().push(ImportCertificatesResultFragmentNav(args.fromShareOption))
    }

    override fun setLoading(isLoading: Boolean) {
        updateLoading(isLoading)
    }

    private fun updateLoading(isLoading: Boolean) {
        binding.loadingLayout.isVisible = isLoading
        binding.infoElementWithList.isVisible = !isLoading
        bottomSheetBinding.bottomSheetActionButton.isVisible = !isLoading
        bottomSheetBinding.bottomSheetHeader.isVisible = !isLoading
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        when (tag) {
            TAG_ERROR_MAX_NUMBER_OF_HOLDERS_EXCEEDED -> {
                binding.recyclerViewCertificateList.isVisible = true
                binding.layoutAllCertificateInfo.isVisible = true
            }
            else -> {
                findNavigator().pop()
            }
        }
    }

    private fun fillInfoElement() {
        binding.infoElementWithList.setValues(
            getString(R.string.file_import_result_info_title),
            null,
            R.drawable.info_icon_update_app,
            R.drawable.info_background,
            listOf(
                getString(R.string.file_import_result_info_bullet1),
                getString(R.string.file_import_result_info_bullet2),
                getString(R.string.file_import_result_info_bullet3),
            ),
            this,
        )
    }

    private fun updateButtonText() {
        if (adapter != null) {
            bottomSheetBinding.bottomSheetActionButton.text = getString(
                R.string.file_import_result_button,
                adapter?.itemCount ?: 0,
            )
        } else {
            bottomSheetBinding.bottomSheetActionButton.setText(R.string.file_import_result_null_button)
        }
    }
}

public class MaxNumberOfHolderExceededException : IllegalStateException()

public data class ImportCovCertificate(
    val covCertificate: CovCertificate,
    val qrContent: String,
)

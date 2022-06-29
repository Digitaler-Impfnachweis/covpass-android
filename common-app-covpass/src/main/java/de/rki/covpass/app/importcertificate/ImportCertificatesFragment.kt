/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.importcertificate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ImportCertificatePopupContentBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import kotlinx.parcelize.Parcelize

public interface ImportCertificateCallback {
    public fun finishedImport()
}

@Parcelize
public class ImportCertificatesFragmentNav : FragmentNav(ImportCertificatesFragment::class)

public class ImportCertificatesFragment : BaseBottomSheet(), DialogListener {

    private val binding by viewBinding(ImportCertificatePopupContentBinding::inflate)

    private var resultFilePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            withErrorReporting {
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        findNavigator().push(ImportCertificatesSelectorFragmentNav(uri))
                        return@withErrorReporting
                    } ?: throw FailedToOpenFileException()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.apply {
            bottomSheetBottomView.isVisible = false
            bottomSheetClose.isVisible = true
            bottomSheetTitle.setText(R.string.file_import_menu_title)
        }

        binding.importButtonGallery.apply {
            setText(R.string.file_import_menu_photo)
            setOnClickListener {
                openPhotoPicker()
            }
        }
        binding.importButtonDocuments.apply {
            setText(R.string.file_import_menu_document)
            setOnClickListener {
                openFilePicker()
            }
        }
    }

    override fun onActionButtonClicked() {}

    private fun openFilePicker() {
        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = "application/pdf"
        chooseFile = Intent.createChooser(chooseFile, getString(R.string.file_import_menu_document))
        chooseFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        resultFilePickerLauncher.launch(chooseFile)
    }

    private fun openPhotoPicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        resultFilePickerLauncher.launch(galleryIntent)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        findNavigator().pop()
    }

    override fun onBackPressed(): Abortable {
        findNavigator().popUntil<ImportCertificateCallback>()?.finishedImport()
        return Abort
    }
}

public class FailedToOpenFileException : IllegalStateException()

/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.commonapp.BaseActivity

internal interface DetailExportPdfSaveEvents : BaseEvents {
    fun onSaveFinished()
}

public class DetailExportPdfSaveOptionActivity : BaseActivity(), DetailExportPdfSaveEvents {

    private val data by lazy { intent.data }
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val outputStream = result.data?.data?.let { contentResolver.openOutputStream(it) }
                val inputStream = data?.let { contentResolver.openInputStream(it) }
                if (inputStream != null && outputStream != null) {
                    detailSavePdfViewModel.copyFile(inputStream, outputStream)
                }
            }
        }

    private val detailSavePdfViewModel by reactiveState { DetailSavePdfViewModel(scope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveFile()
    }

    private fun saveFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_TITLE, (data as Uri).encodedPath?.removePrefix("/tempfile/"))
        startForResult.launch(Intent(intent))
    }

    override fun onSaveFinished() {
        finish()
    }
}

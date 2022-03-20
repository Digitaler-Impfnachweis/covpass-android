/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.revocation

import android.util.Base64
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.commonapp.pdfexport.BaseExportPdfViewModel
import de.rki.covpass.sdk.cert.models.ExpertModeData
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.RevocationCodeEncryptor
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.sanitizeFileName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import java.time.Instant

internal class RevocationExportViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val revocationCodeEncryptor: RevocationCodeEncryptor = sdkDeps.revocationCodeEncryptor
) : BaseExportPdfViewModel(scope) {

    private val fileName: MutableStateFlow<String> = MutableStateFlow("")

    override fun getFileName(): String = "${fileName.value}.pdf".sanitizeFileName()

    fun onShareClick(data: ExpertModeData) {
        launch {
            val encryptedData = revocationCodeEncryptor.encrypt(defaultJson.encodeToString(data))
            val base64EncodedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)
            fileName.value = "${data.transactionNumber}_${data.issuingCountry}_${Instant.now().formatDateOrEmpty()}"
            pdfString.value = PdfUtils.replaceTechnicalDetailsValues(
                applicationContext,
                data,
                base64EncodedData,
                base64EncodedData.toBase64EncodedString()
            )
        }
    }
}

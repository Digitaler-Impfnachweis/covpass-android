/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.pdfexport

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.print.PdfBuilder
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import androidx.core.content.FileProvider
import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.androidDeps
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.rki.covpass.commonapp.dependencies.commonDeps
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Interface to communicate events from [BaseExportPdfViewModel].
 */
public interface SharePdfEvents : BaseEvents {
    public fun onSharePdf(uri: Uri)
}

/**
 * ViewModel to handle pdf export logic.
 */
public abstract class BaseExportPdfViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    public val applicationContext: Context = androidDeps.application,
    private val providerAuthority: String = commonDeps.fileProviderAuthority,
) : BaseReactiveState<SharePdfEvents>(scope) {

    public val pdfString: MutableStateFlow<String> = MutableStateFlow("")

    public abstract fun getFileName(): String

    public fun onShareStart(printDocumentAdapter: PrintDocumentAdapter) {
        launch {
            val attributes = PrintAttributes.Builder()
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .build()
            val fileName = getFileName()
            val pdfFile = File(applicationContext.cacheDir, fileName)
            PdfBuilder(attributes).createPdf(printDocumentAdapter, pdfFile)
            eventNotifier { onSharePdf(uriFromFile(applicationContext, pdfFile)) }
        }
    }

    private fun uriFromFile(context: Context, file: File): Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context, providerAuthority, file)
    } else {
        Uri.fromFile(file)
    }

    public fun String.toBase64EncodedString(): String {
        return BarcodeEncoder().encodeBitmap(
            this,
            BarcodeFormat.QR_CODE,
            619,
            619,
            mapOf(EncodeHintType.MARGIN to 0)
        ).convertToPngAndEncodeBase64()
    }

    private fun Bitmap.convertToPngAndEncodeBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray().encodeBase64()
    }
}

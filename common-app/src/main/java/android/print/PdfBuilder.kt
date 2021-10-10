/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package android.print

import android.os.CancellationSignal
import de.rki.covpass.commonapp.utils.toParcelFileDescriptor
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

public class PdfBuilder(private val attributes: PrintAttributes) {

    public suspend fun createPdf(printAdapter: PrintDocumentAdapter, file: File) {
        suspendCoroutine<Unit> { continuation ->
            printAdapter.onLayout(
                null,
                attributes,
                null,
                object : PrintDocumentAdapter.LayoutResultCallback() {
                    override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                        printAdapter.onWrite(
                            arrayOf(PageRange.ALL_PAGES),
                            file.toParcelFileDescriptor(),
                            CancellationSignal(),
                            object : PrintDocumentAdapter.WriteResultCallback() {
                                override fun onWriteFinished(pages: Array<out PageRange>?) {
                                    super.onWriteFinished(pages)
                                    if (pages.isNullOrEmpty()) {
                                        continuation.resumeWithException(NoPagesAvailableException())
                                    } else {
                                        continuation.resume(Unit)
                                    }
                                }
                            }
                        )
                    }
                },
                null
            )
        }
    }

    public class NoPagesAvailableException(override val message: String? = null) : Exception()
}

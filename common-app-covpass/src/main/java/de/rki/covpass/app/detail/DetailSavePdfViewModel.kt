/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.dispatchers
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream

internal class DetailSavePdfViewModel(
    scope: CoroutineScope,
) : BaseReactiveState<DetailExportPdfSaveEvents>(scope) {

    fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        launch(dispatchers.io) {
            outputStream.use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }
            eventNotifier { onSaveFinished() }
        }
    }
}

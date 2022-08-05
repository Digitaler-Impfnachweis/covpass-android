/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.scanner

import androidx.camera.core.ImageProxy
import boofcv.struct.image.GrayU8
import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.dispatchers
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.sdk.utils.parallelMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

public class QRScannerViewModel(scope: CoroutineScope) : BaseReactiveState<BaseEvents>(scope) {
    private val incomingImages: MutableSharedFlow<ImageProxy> = MutableSharedFlow(extraBufferCapacity = 1)
    public val scanResults: MutableSharedFlow<String> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val detectors: List<(GrayU8) -> List<String>> = listOf(::detectQr, ::detectQrTransposed)

    init {
        launch(dispatchers.default, withLoading = null) {
            incomingImages.collect {
                it.use { imageProxy ->
                    val image = imageProxy.toGrayU8()
                    detectors.parallelMap { detector ->
                        for (qr in detector(image)) {
                            scanResults.tryEmit(qr)
                        }
                    }
                }
            }
        }
    }

    public fun onNewImage(image: ImageProxy) {
        if (!incomingImages.tryEmit(image)) {
            image.close()
        }
    }
}

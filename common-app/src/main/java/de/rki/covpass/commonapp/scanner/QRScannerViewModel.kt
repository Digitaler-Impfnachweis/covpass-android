/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import boofcv.alg.color.ColorFormat
import boofcv.android.ConvertCameraImage
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageType
import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.dispatchers
import com.ibm.health.common.android.utils.BaseEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

@SuppressLint("UnsafeOptInUsageError")
public class QRScannerViewModel(scope: CoroutineScope) : BaseReactiveState<BaseEvents>(scope) {
    private val incomingImages: MutableSharedFlow<ImageProxy> = MutableSharedFlow(extraBufferCapacity = 1)
    public val scanResults: MutableSharedFlow<String> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        launch(dispatchers.default, withLoading = null) {
            incomingImages.collect { image ->
                image.use {
                    val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)
                    val imageBase = ImageType.SB_U8.createImage(1, 1)

                    ConvertCameraImage.imageToBoof(image.image, ColorFormat.RGB, imageBase, null)
                    detector.process(imageBase)

                    for (qr in detector.detections) {
                        scanResults.tryEmit(qr.message)
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

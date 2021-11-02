package de.rki.covpass.commonapp.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import boofcv.alg.color.ColorFormat
import boofcv.android.ConvertCameraImage
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageType

internal fun GrayU8.transpose(): GrayU8 {
    val transposed = ImageType.SB_U8.createImage(height, width)
    for (x in 0 until width) {
        for (y in 0 until height) {
            transposed[y, x] = this[x, y]
        }
    }
    return transposed
}

@SuppressLint("UnsafeOptInUsageError")
internal fun ImageProxy.toGrayU8(): GrayU8 =
    ImageType.SB_U8.createImage(width, height).also {
        ConvertCameraImage.imageToBoof(image, ColorFormat.RGB, it, null)
    }

internal fun detectQrTransposed(image: GrayU8): List<String> =
    detectQr(image.transpose())

internal fun detectQr(image: GrayU8): List<String> {
    val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)
    detector.process(image)
    return detector.detections.map { it.message }
}

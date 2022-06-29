/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.importcertificate

import android.graphics.Bitmap
import boofcv.android.ConvertBitmap
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.struct.image.GrayU8

public fun Bitmap.convertToQrContentList(): List<String> {
    // Easiest way to convert a Bitmap into a BoofCV type
    val image: GrayU8 = ConvertBitmap.bitmapToGray(this, null as GrayU8?, null)

    val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)
    detector.process(image)

    return detector.detections.map { it.message }
}

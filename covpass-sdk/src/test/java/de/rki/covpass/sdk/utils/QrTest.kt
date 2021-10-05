package de.rki.covpass.sdk.utils

import boofcv.factory.fiducial.FactoryFiducial
import boofcv.struct.image.GrayU8
import de.rki.covpass.sdk.cert.CertValidator
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.TrustedCert
import de.rki.covpass.sdk.cert.models.CBORWebToken
import de.rki.covpass.sdk.crypto.readPem
import de.rki.covpass.sdk.dependencies.defaultCbor
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull

/** A class that decodes the raw ARGB [pixels] for the given image [file]. */
internal class Image(private val file: File) {
    val width: Int
    val height: Int
    val pixels: IntArray

    init {
        // XXX:
        //  In Android unit tests BitmapFactory doesn't work correctly and reads all-white images.
        //  ImageIO can't be imported, but reflection works and it's able to read images correctly.
        val image = readMethod.invoke(imageIO, file.toURI().toURL())
        val getWidth = image::class.java.getDeclaredMethod("getWidth")
        val getHeight = image::class.java.getDeclaredMethod("getHeight")
        val getRGB = image::class.java.getDeclaredMethod("getRGB", Int::class.java, Int::class.java)
        width = getWidth.invoke(image) as Int
        height = getHeight.invoke(image) as Int
        pixels = IntArray(width * height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                pixels[x + width * y] = getRGB.invoke(image, x, y) as Int
            }
        }
    }

    companion object {
        val imageIO = Class.forName("javax.imageio.ImageIO")
        val readMethod = imageIO.getDeclaredMethod("read", java.net.URL::class.java)
    }
}

internal fun createEmptyGrayU8(width: Int, height: Int): GrayU8 {
    val data = GrayU8(width, height)
    for (y in 0 until data.height) {
        for (x in 0 until data.width) {
            data[x, y] = 0xffffffffU.toInt()
        }
    }
    return data
}

internal fun Image.decodeQr(): String? {
    // XXX: Add a white border around the image because BoofCV otherwise fails finding the QR code
    val border = 15
    val data = createEmptyGrayU8(width + 2 * border, height + 2 * border)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val argb = pixels[x + y * width].toUInt()
            data[x + border, y + border] =
                if (argb and 0xff000000U == 0U) 0xffffffffU.toInt() else argb.toInt()
        }
    }

    val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)
    detector.process(data)

    for (qr in detector.detections) {
        return qr.message
    }
    return null
}

internal fun File.isImage() =
    listOf(".png", ".jpg", ".jpeg").any { name.endsWith(it) }

internal class QrTest {

    private val sealCert by lazy { readPem(readResource("seal-cert.pem")).first() }
    private val validator by lazy {
        CertValidator(
            listOf(TrustedCert(country = "DE", kid = "asdf", certificate = sealCert)),
            defaultCbor,
        )
    }
    private val qrCoder: QRCoder by lazy { QRCoder(validator) }

    @Test
    fun `test all dcc-quality-assurance QRcodes`() {
        val path = File("../dcc-quality-assurance")
        lookForImagesAndDecode(path)
    }

    private fun lookForImagesAndDecode(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach(::lookForImagesAndDecode)
        } else if (file.isFile && file.isImage()) {
            val result = Image(file).decodeQr()
            assertNotNull(result)
            val cose = qrCoder.decodeCose(result)
            validator.decodeAndValidate(CBORWebToken.decode(cose.GetContent()), sealCert)
        }
    }
}

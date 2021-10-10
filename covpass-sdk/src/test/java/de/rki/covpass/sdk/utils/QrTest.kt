package de.rki.covpass.sdk.utils

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import de.rki.covpass.sdk.cert.CertValidator
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.TrustedCert
import de.rki.covpass.sdk.cert.models.CBORWebToken
import de.rki.covpass.sdk.crypto.readPem
import de.rki.covpass.sdk.dependencies.defaultCbor
import java.io.File
import java.util.*
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

internal fun Image.toBinaryBitmap(): BinaryBitmap {
    // XXX: The images can contain transparent areas which we need to turn into white background,
    // so the binary bitmap doesn't highlight those.
    val data = IntArray(width * height)
    for (index in pixels.indices) {
        val argb = pixels[index].toUInt()
        data[index] = if (argb and 0xff000000U == 0U) 0xffffffffU.toInt() else argb.toInt()
    }
    val source = RGBLuminanceSource(width, height, data)
    return BinaryBitmap(HybridBinarizer(source))
}

internal fun Image.decodeQr(): Result? {
    val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
    hints[DecodeHintType.TRY_HARDER] = true
    hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(
        BarcodeFormat.QR_CODE,
        BarcodeFormat.AZTEC,
        BarcodeFormat.DATA_MATRIX,
    )
    return MultiFormatReader().decode(toBinaryBitmap(), hints)
}

internal fun File.isImage() = this.name.endsWith(".png") || this.name.endsWith(".jpg") || this.name.endsWith(".jpeg")

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
            try {
                val result = Image(file).decodeQr()
                val cose = result?.text?.let { qrCoder.decodeCose(it) }
                assertNotNull(cose)
                validator.decodeAndValidate(CBORWebToken.decode(cose.GetContent()), sealCert)
            } catch (e: NotFoundException) {
                println("-------------Fail------------")
                println("name: ${file.path}")
                println(e.stackTraceToString())
            }
        }
    }
}

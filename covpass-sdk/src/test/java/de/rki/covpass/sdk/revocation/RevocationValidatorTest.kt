/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

import com.ensody.reactivestate.test.CoroutineTest
import de.rki.covpass.sdk.cert.models.CovCertificate
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RevocationValidatorTest : CoroutineTest() {

    private val kid = "9cWXDDA52FQ="
    private val rValue = "aa9ffbe3461d38427198aeaefc1f87df04c1055f28163bd734de983855c70c29".decodeHexToByteArray()
    private val id = "URN:UVCI:V1:DE:MNI5SHBAVDC5JWF0WI63I5IQ68"
    private val issuer = "DE"
    private val cert: CovCertificate = mockk()
    private val revocationRemoteListRepository: RevocationRemoteListRepository = mockk()
    private val kidList = listOf(
        RevocationKidEntry(
            "ea3ab2264f346d45".decodeHexToByteArray(),
            mapOf(
                Pair(0x0a, 538)
            )
        ),
        RevocationKidEntry(
            "f5c5970c3039d854".decodeHexToByteArray(),
            mapOf(
                Pair(0x0a, 7),
                Pair(0x0b, 1),
                Pair(0x0c, 1)
            )
        ),
        RevocationKidEntry(
            "f50159a32d84e89d".decodeHexToByteArray(),
            mapOf(
                Pair(0x0b, 5)
            )
        ),
    )
    private val indexList = mapOf(
        Pair(
            "a6".decodeHexToByteArray().first(),
            RevocationIndexEntry(
                1646129242,
                1,
                mapOf(
                    Pair(
                        "b8".decodeHexToByteArray().first(),
                        RevocationIndexByte2Entry(1646129242, 1)
                    )
                )

            )
        ),
        Pair(
            "b4".decodeHexToByteArray().first(),
            RevocationIndexEntry(
                1646044012,
                1,
                mapOf(
                    Pair(
                        "65".decodeHexToByteArray().first(),
                        RevocationIndexByte2Entry(1646044012, 1)
                    )
                )

            )
        ),
        Pair(
            "ec".decodeHexToByteArray().first(),
            RevocationIndexEntry(
                1646129243,
                1,
                mapOf(
                    Pair(
                        "e1".decodeHexToByteArray().first(),
                        RevocationIndexByte2Entry(1646129243, 1)
                    )
                )

            )
        ),
        Pair(
            "ee".decodeHexToByteArray().first(),
            RevocationIndexEntry(
                1649087075,
                1,
                mapOf(
                    Pair(
                        "36".decodeHexToByteArray().first(),
                        RevocationIndexByte2Entry(1649087075, 1)
                    )
                )

            )
        ),
    )

    private val chunkListByte1 = listOf("a6b8a01b67030f32e0e3d7052a71a688".decodeHexToByteArray())
    private val chunkListByte2 = listOf("a6b8a01b67030f32e0e3d7052a71a688".decodeHexToByteArray())

    init {
        // covCertificate mockk
        coEvery { cert.dgcEntry.id } returns id
        coEvery { cert.kid } returns kid
        coEvery { cert.getRValueByteArray } returns rValue
        coEvery { cert.issuer } returns issuer

        // revocationListRepository
        coEvery { revocationRemoteListRepository.getKidList() } returns kidList
        coEvery { revocationRemoteListRepository.getIndex(any(), any()) } returns indexList
        coEvery { revocationRemoteListRepository.getByteOneChunk(any(), any(), any()) } returns chunkListByte1
        coEvery { revocationRemoteListRepository.getByteTwoChunk(any(), any(), any(), any()) } returns chunkListByte2
    }

    @Test
    fun `test empty kid list revocation`() = runBlockingTest {

        coEvery {
            revocationRemoteListRepository.getKidList()
        } returns emptyList()
        assertFalse(validateRevocation(cert, revocationRemoteListRepository))
    }

    @Test
    fun `test empty index list revocation`() = runBlockingTest {

        coEvery {
            revocationRemoteListRepository.getIndex(any(), any())
        } returns emptyMap()
        assertFalse(validateRevocation(cert, revocationRemoteListRepository))
    }

    @Test
    fun `test empty chunk one and two list revocation`() = runBlockingTest {
        coEvery {
            revocationRemoteListRepository.getByteOneChunk(any(), any(), any())
        } returns emptyList()
        coEvery {
            revocationRemoteListRepository.getByteTwoChunk(any(), any(), any(), any())
        } returns emptyList()
        assertFalse(validateRevocation(cert, revocationRemoteListRepository))
    }

    @Test
    fun `test empty chunk one list revocation`() = runBlockingTest {
        coEvery {
            revocationRemoteListRepository.getByteOneChunk(any(), any(), any())
        } returns emptyList()
        assertTrue(validateRevocation(cert, revocationRemoteListRepository))
    }

    @Test
    fun `test empty chunk two list revocation`() = runBlockingTest {
        coEvery {
            revocationRemoteListRepository.getByteTwoChunk(any(), any(), any(), any())
        } returns emptyList()
        assertTrue(validateRevocation(cert, revocationRemoteListRepository))
    }

    @Test
    fun `test full revocation`() = runBlockingTest {
        assertTrue(validateRevocation(cert, revocationRemoteListRepository))
    }

    private fun String.decodeHexToByteArray(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}

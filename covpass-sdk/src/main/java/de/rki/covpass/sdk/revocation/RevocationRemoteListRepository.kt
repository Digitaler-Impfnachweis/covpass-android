/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

import COSE.OneKey
import COSE.Sign1Message
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.upokecenter.cbor.CBORObject
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.DscRepository.Companion.NO_UPDATE_YET
import de.rki.covpass.sdk.utils.isNetworkError
import de.rki.covpass.sdk.utils.toHex
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.host
import kotlinx.serialization.Serializable
import okhttp3.Cache
import java.io.File
import java.security.PublicKey
import java.time.Instant

public class RevocationRemoteListRepository(
    httpClient: HttpClient,
    host: String,
    store: CborSharedPrefsStore,
    cacheDir: File,
    private val revocationListPublicKey: PublicKey,
    private val revocationLocalListRepository: RevocationLocalListRepository,
) {

    @Suppress("UNCHECKED_CAST")
    private val client = httpClient.config {
        (this as? HttpClientConfig<OkHttpConfig>)?.engine {
            preconfigured = preconfigured?.newBuilder()
                ?.cache(
                    Cache(
                        directory = File(cacheDir, "http_cache"),
                        maxSize = 50L * 1024L * 1024L, // 50 MiB
                    ),
                )?.build()
        }
        defaultRequest {
            this.host = host
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(defaultJson)
        }
    }

    public val lastRevocationValidation: SuspendMutableValueFlow<Instant> =
        store.getData("last_revocation_validation", NO_UPDATE_YET)

    /**
     * Update the [lastRevocationValidation].
     */
    public suspend fun updateLastRevocationValidation() {
        lastRevocationValidation.set(Instant.now())
    }

    public suspend fun getKidList(): List<RevocationKidEntry> {
        return if (revocationLocalListRepository.revocationListUpdateIsOn.value) {
            revocationLocalListRepository.getSavedKidList().toListOfRevocationKidEntry()
        } else {
            val cborObject = getSigned("kid.lst")
            cborObject?.toKidList() ?: emptyList()
        }
    }

    public suspend fun getIndex(kid: ByteArray, hashType: Byte): Map<Byte, RevocationIndexEntry> {
        return if (revocationLocalListRepository.revocationListUpdateIsOn.value) {
            revocationLocalListRepository.getSavedIndex(kid, hashType)
        } else {
            val cborObject = getSigned("${kid.toHex()}${hashType.toHex()}/index.lst")
            cborObject?.toIndexResponse() ?: emptyMap()
        }
    }

    public suspend fun getByteOneChunk(
        kid: ByteArray,
        hashType: Byte,
        byte1: Byte,
    ): List<ByteArray> {
        return if (revocationLocalListRepository.revocationListUpdateIsOn.value) {
            revocationLocalListRepository.getSavedByteOneChunk(kid, hashType, byte1)
        } else {
            val cborObject = getSigned("${kid.toHex()}${hashType.toHex()}/${byte1.toHex()}/chunk.lst")
            cborObject?.toListOfByteArrays() ?: emptyList()
        }
    }

    public suspend fun getByteTwoChunk(
        kid: ByteArray,
        hashType: Byte,
        byte1: Byte,
        byte2: Byte,
    ): List<ByteArray> {
        return if (revocationLocalListRepository.revocationListUpdateIsOn.value) {
            revocationLocalListRepository.getSavedByteTwoChunk(kid, hashType, byte1, byte2)
        } else {
            val cborObject =
                getSigned("${kid.toHex()}${hashType.toHex()}/${byte1.toHex()}/${byte2.toHex()}/chunk.lst")
            cborObject?.toListOfByteArrays() ?: emptyList()
        }
    }

    private suspend fun getSigned(url: String): CBORObject? {
        return try {
            val list: ByteArray = client.get(url)
            val sign1Message = Sign1Message.DecodeFromBytes(list) as Sign1Message
            validateRevocationSignature(sign1Message)
            CBORObject.DecodeFromBytes(sign1Message.GetContent())
        } catch (e: RevocationListSignatureValidationFailedException) {
            null
        } catch (e: Throwable) {
            if (isNetworkError(e)) {
                null
            } else {
                throw e
            }
        }
    }

    private fun validateRevocationSignature(sign1Message: Sign1Message) {
        if (!sign1Message.validate(OneKey(revocationListPublicKey, null))) {
            throw RevocationListSignatureValidationFailedException()
        }
    }

    public class RevocationListSignatureValidationFailedException : Exception()
}

public data class RevocationKidEntry(
    val kid: ByteArray,
    val hashVariants: Map<Byte, Int>,
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false

        other as RevocationKidEntry

        if (!kid.contentEquals(other.kid)) return false
        if (hashVariants != other.hashVariants) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kid.contentHashCode()
        result = 31 * result + hashVariants.hashCode()
        return result
    }
}

@Serializable
public data class RevocationIndexEntry(
    val timestamp: Long?,
    val num: Int?,
    val byte2: Map<Byte, RevocationIndexByte2Entry>?,
)

@Serializable
public data class RevocationIndexByte2Entry(
    val timestamp: Long?,
    val num: Int?,
)

public class NetworkErrorOnOfflineMode : IllegalStateException()

package de.rki.covpass.sdk.utils

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

public class HeaderInterceptor : Interceptor {

    private val userAgent = "DGCA verifier Android ${Build.VERSION.SDK_INT}, ${Build.MODEL};"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = addHeadersToRequest(chain.request())

        return chain.proceed(request)
    }

    private fun addHeadersToRequest(original: Request): Request {
        val requestBuilder = original.newBuilder()
            .header("User-Agent", userAgent)

        return requestBuilder.build()
    }
}

package com.ibm.health.sampleapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ibm.health.common.security.android.webview.SafeWebViewClient
import com.ibm.health.common.security.android.webview.initSafe
import com.ibm.health.ui.util.IntentNav
import com.ibm.health.ui.util.getArgs
import io.ktor.client.statement.HttpResponse
import io.ktor.http.URLParserException
import io.ktor.http.Url
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.sample_login_activity.*

@Parcelize
class SampleKeycloakActivityNav(val authUrl: String) : IntentNav(SampleKeycloakActivity::class.java)

class SampleKeycloakActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_login_activity)
        register()
    }

    private fun register() {
        // XXX: Loading registration URL directly fails. We have to first load authUrl to init the cookies.
        webview.isVisible = false
        launchWebView(
            getArgs<SampleKeycloakActivityNav>().authUrl,
            object : SafeWebViewClient {
                val onFinishedExecOnce = ExecOnce()

                override val javaScriptEnabled: Boolean = true

                override suspend fun onResponse(response: HttpResponse, isPageLoad: Boolean): Boolean =
                    try {
                        if ("code" in Url(response.headers["Location"] ?: "").parameters) {
                            finish()
                            false
                        } else true
                    } catch (e: URLParserException) {
                        true
                    }

                override fun onPageFinished(view: WebView, url: String) {
                    // Once the authUrl is loaded, we can redirect to registration page
                    onFinishedExecOnce {
                        view.evaluateJavascript(
                            """
                            document.querySelector('#kc-registration a').click()
                            """.trimIndent()
                        ) {}
                    }

                    // XXX: Remove "Back to login" link
                    if (!url.contains("openid-connect/auth")) {
                        view.evaluateJavascript(
                            """
                            for (let link of document.querySelectorAll('#kc-form-options a')) {
                                if (link.text.toLowerCase().includes('login')) {
                                    link.remove()
                                }
                            }
                            """.trimIndent()
                        ) {}
                        view.isVisible = true
                    }
                }
            }
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun launchWebView(url: String, safeWebViewClient: SafeWebViewClient) {
        webview.initSafe(safeWebViewClient)
        webview.loadUrl(url)
    }

    companion object {
        // TODO: Maybe move to common utils module?
        private class ExecOnce {
            private var executed = false

            operator fun invoke(func: () -> Unit) {
                if (!executed) {
                    try {
                        func()
                    } finally {
                        executed = true
                    }
                }
            }
        }
    }
}

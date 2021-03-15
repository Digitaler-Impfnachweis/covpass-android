package com.ibm.health.vaccination.app

import android.app.Application
import android.webkit.WebView
import com.ensody.reactivestate.BuildConfig
import com.ibm.health.common.http.HttpLogLevel
import com.ibm.health.common.http.httpConfig
import com.ibm.health.common.logging.Lumber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Lumber.plantDebugTreeIfNeeded()
            // Make sure not to commit HttpLogLevel.BODY, it'll break the document
            // upload since the byte stream will be logged sequentially and due to the slow performance
            // of the stdout stream the request will get canceled
            httpConfig.enableLogging(HttpLogLevel.HEADERS)
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }
}

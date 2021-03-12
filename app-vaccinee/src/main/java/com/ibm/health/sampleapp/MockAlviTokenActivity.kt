package com.ibm.health.sampleapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ibm.health.ui.util.IntentNav
import com.ibm.health.ui.util.getArgs
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MockAlviTokenActivityNav(val config: String) : IntentNav(MockAlviTokenActivity::class.java)

/**
 * Mock activity for receiving an alvi token. Will be replaced by real implementation of login activity from host app.
 */
class MockAlviTokenActivity : AppCompatActivity() {

    val args by lazy { getArgs<MockAlviTokenActivityNav>() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.gravity = Gravity.CENTER
        layout.orientation = LinearLayout.VERTICAL

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val textView = TextView(this)
        textView.text = "Mock Alvi Token Login for config: ${args.config}"
        layout.addView(textView, params)

        val buttonSuccess = Button(this)
        buttonSuccess.text = "Press to login successfully"
        buttonSuccess.setOnClickListener {
            HealthAuthService.reportSuccess("success", this)
        }
        layout.addView(buttonSuccess, params)

        val buttonError = Button(this)
        buttonError.text = "Press to login with error"
        buttonError.setOnClickListener {
            HealthAuthService.reportSuccess("error", this)
        }
        layout.addView(buttonError, params)

        val buttonUnknown = Button(this)
        buttonUnknown.text = "Press to login with device unknown"
        buttonUnknown.setOnClickListener {
            HealthAuthService.reportSuccess("device_unknown", this)
        }
        layout.addView(buttonUnknown, params)

        setContentView(layout)
    }
}

package com.ibm.health.sampleapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.mock_host_login_activity.*

class MockHostLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mock_host_login_activity)

        login.setOnClickListener {
            MockAuthService.lastLoginInMillis = System.currentTimeMillis()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, MockHostLoginActivity::class.java)
    }
}

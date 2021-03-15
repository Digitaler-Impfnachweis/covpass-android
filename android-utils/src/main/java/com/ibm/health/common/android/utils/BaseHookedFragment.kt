package com.ibm.health.common.android.utils

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

/** Base class that comes with mixin hook support. */
public abstract class BaseHookedFragment :
    Fragment(),
    OnCreateHook,
    OnActivityResultHook,
    OnRequestPermissionsResultHook {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateHook()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultHook(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResultHook(requestCode, permissions, grantResults)
    }
}

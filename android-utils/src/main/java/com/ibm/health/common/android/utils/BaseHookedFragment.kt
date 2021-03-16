package com.ibm.health.common.android.utils

import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/** Base class that comes with mixin hook support. */
public abstract class BaseHookedFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId),
    OnActivityResultHook,
    OnRequestPermissionsResultHook,
    LoadingStateHook {

    override val isLoading: IsLoading = IsLoading()

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

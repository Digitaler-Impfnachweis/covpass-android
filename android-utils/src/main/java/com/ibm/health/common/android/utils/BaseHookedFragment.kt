package com.ibm.health.common.android.utils

import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/** Base class that comes with hook support. */
public abstract class BaseHookedFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId),
    OnActivityResultHook,
    LoadingStateHook {

    override val isLoading: IsLoading = IsLoading()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultHook(requestCode, resultCode, data)
    }
}

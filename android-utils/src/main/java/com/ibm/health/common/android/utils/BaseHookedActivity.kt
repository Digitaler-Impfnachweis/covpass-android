package com.ibm.health.common.android.utils

import android.content.Intent
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

/** Base class that comes with mixin hook support. */
public abstract class BaseHookedActivity(@LayoutRes contentLayoutId: Int = 0) :
    AppCompatActivity(contentLayoutId),
    OnActivityResultHook,
    OnRequestPermissionsResultHook,
    LoadingStateHook {

    override val isLoading: IsLoading = IsLoading()

    /** Helper to abstract away activity and fragment differences. */
    public open fun requireActivity(): FragmentActivity = this

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

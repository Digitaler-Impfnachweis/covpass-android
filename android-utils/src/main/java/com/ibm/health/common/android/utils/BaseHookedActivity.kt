package com.ibm.health.common.android.utils

import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

/** Base class that comes with hook support. */
public abstract class BaseHookedActivity(@LayoutRes contentLayoutId: Int = 0) :
    AppCompatActivity(contentLayoutId),
    LoadingStateHook {

    override val isLoading: IsLoading = IsLoading()

    /** Helper to abstract away activity and fragment differences. */
    public open fun requireActivity(): FragmentActivity = this

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

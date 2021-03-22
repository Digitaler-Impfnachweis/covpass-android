package com.ibm.health.common.android.utils

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding

/** Base class that comes with hook support. */
public abstract class BaseHookedActivity(@LayoutRes contentLayoutId: Int = 0) :
    AppCompatActivity(contentLayoutId),
    LoadingStateHook,
    BaseEvents {

    /** Override this to define a [ViewBinding] that is automatically inflated. */
    public open val binding: ViewBinding? = null

    override val isLoading: IsLoading = IsLoading()

    /** Helper to abstract away activity and fragment differences. */
    public open fun requireActivity(): FragmentActivity = this

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding?.also {
            setContentView(it.root)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

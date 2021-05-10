package com.ibm.health.common.vaccination.app

import androidx.annotation.LayoutRes
import com.ibm.health.common.android.utils.BaseHookedFragment
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.NavigatorOwner
import com.ibm.health.common.navigation.android.OnBackPressedNavigation
import com.ibm.health.common.vaccination.app.dependencies.commonDeps

/** Common base fragment with some common functionality like error handling or loading behaviour. */
public abstract class BaseFragment(@LayoutRes contentLayoutId: Int = 0) :
    BaseHookedFragment(contentLayoutId = contentLayoutId),
    OnBackPressedNavigation {

    override fun onBackPressed(): Abortable =
        (this as? NavigatorOwner)?.navigator?.onBackPressed()
            ?: Continue

    override fun onError(error: Throwable) {
        commonDeps.errorHandler.handleError(error, childFragmentManager)
    }

    override fun setLoading(isLoading: Boolean) {}
}

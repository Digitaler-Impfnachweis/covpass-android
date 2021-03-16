package com.ibm.health.common.vaccination.app

import androidx.annotation.LayoutRes
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseHookedActivity
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.Navigator
import com.ibm.health.common.navigation.android.NavigatorOwner

public abstract class BaseActivity(@LayoutRes contentLayoutId: Int = 0) :
    BaseHookedActivity(contentLayoutId = contentLayoutId),
    NavigatorOwner,
    BaseEvents {

    override val navigator: Navigator = Navigator()

    override fun onBackPressed() {
        if (navigator.onBackPressed() == Continue) {
            super.onBackPressed()
        }
    }

    override fun onError(error: Throwable) {
        handleError(error, supportFragmentManager)
    }

    override fun setLoading(isLoading: Boolean) {
        // TODO: Implement this
    }
}

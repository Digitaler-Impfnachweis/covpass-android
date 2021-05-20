/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.vaccination.app

import androidx.annotation.LayoutRes
import com.ibm.health.common.android.utils.BaseHookedActivity
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.Navigator
import com.ibm.health.common.navigation.android.NavigatorOwner
import com.ibm.health.common.vaccination.app.dependencies.commonDeps

/** Common base activity with some common functionality like error handling or loading behaviour. */
public abstract class BaseActivity(@LayoutRes contentLayoutId: Int = 0) :
    BaseHookedActivity(contentLayoutId = contentLayoutId),
    NavigatorOwner {

    override val navigator: Navigator = Navigator()

    override fun onBackPressed() {
        if (navigator.onBackPressed() == Continue) {
            super.onBackPressed()
        }
    }

    override fun onError(error: Throwable) {
        commonDeps.errorHandler.handleError(error, supportFragmentManager)
    }

    override fun setLoading(isLoading: Boolean) {}
}

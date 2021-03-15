package com.ibm.health.common.vaccination.app

import com.ibm.health.common.android.utils.BaseHookedActivity
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.Navigator
import com.ibm.health.common.navigation.android.NavigatorOwner

public abstract class BaseActivity : BaseHookedActivity(), NavigatorOwner {
    override val navigator: Navigator = Navigator()

    override fun onBackPressed() {
        if (navigator.onBackPressed() == Continue) {
            super.onBackPressed()
        }
    }
}

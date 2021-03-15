package com.ibm.health.common.vaccination.app

import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.NavigatorOwner
import com.ibm.health.common.navigation.android.OnBackPressedNavigation
import com.ibm.health.common.android.utils.BaseHookedFragment

public open class BaseFragment : BaseHookedFragment(), OnBackPressedNavigation {
    override fun onBackPressed(): Abortable =
        (this as? NavigatorOwner)?.navigator?.onBackPressed()
            ?: Continue
}

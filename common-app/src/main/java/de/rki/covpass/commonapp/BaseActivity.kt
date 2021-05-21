/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.os.Bundle
import androidx.annotation.LayoutRes
import com.ibm.health.common.android.utils.BaseHookedActivity
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.Navigator
import com.ibm.health.common.navigation.android.NavigatorOwner
import de.rki.covpass.commonapp.databinding.MainActivityBinding
import de.rki.covpass.commonapp.dependencies.commonDeps

/** Common base activity with some common functionality like error handling or loading behaviour. */
public abstract class BaseActivity(@LayoutRes contentLayoutId: Int = 0) :
    BaseHookedActivity(contentLayoutId = contentLayoutId),
    NavigatorOwner {

    override val navigator: Navigator = Navigator(R.id.fragment_container)

    override val binding: MainActivityBinding by viewBinding(MainActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.mainToolbar)
    }

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

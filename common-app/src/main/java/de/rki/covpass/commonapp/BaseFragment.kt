/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.ibm.health.common.android.utils.BaseHookedFragment
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.NavigatorOwner
import com.ibm.health.common.navigation.android.OnBackPressedNavigation
import de.rki.covpass.commonapp.dependencies.commonDeps

/** Common base fragment with some common functionality like error handling or loading behaviour. */
public abstract class BaseFragment(@LayoutRes contentLayoutId: Int = 0) :
    BaseHookedFragment(contentLayoutId = contentLayoutId),
    OnBackPressedNavigation {

    public open val showActionBar: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (showActionBar) {
            (activity as? AppCompatActivity)?.supportActionBar?.show()
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.hide()
        }
    }

    override fun onBackPressed(): Abortable =
        (this as? NavigatorOwner)?.navigator?.onBackPressed()
            ?: Continue

    override fun onError(error: Throwable) {
        commonDeps.errorHandler.handleError(error, childFragmentManager)
    }

    override fun setLoading(isLoading: Boolean) {}
}

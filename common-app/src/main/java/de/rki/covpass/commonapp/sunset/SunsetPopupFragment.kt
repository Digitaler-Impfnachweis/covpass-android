/*
 * (C) Copyright IBM Deutschland GmbH 2023
 * (C) Copyright IBM Corp. 2023
 */

package de.rki.covpass.commonapp.sunset

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.SunsetPopupBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.utils.isLandscapeMode
import kotlinx.parcelize.Parcelize

public interface SunsetPopupCallback {
    public fun onSunsetPopupFinish()
}

@Parcelize
public class SunsetPopupFragmentNav(
    public val isCovPassCheck: Boolean = false,
) : FragmentNav(SunsetPopupFragment::class)

internal class SunsetPopupFragment : BaseBottomSheet() {

    private val args by lazy { getArgs<SunsetPopupFragmentNav>() }
    private val binding by viewBinding(SunsetPopupBinding::inflate)
    override val buttonTextRes: Int = R.string.ok

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sunsetIllustration.isGone = resources.isLandscapeMode()
        binding.sunsetBulletContainer5.isGone = args.isCovPassCheck
        binding.sunsetBulletContainer6.isGone = args.isCovPassCheck
        binding.sunsetBulletContainer7.isGone = args.isCovPassCheck
        binding.sunsetBulletContainer8.isGone = args.isCovPassCheck
        binding.sunsetBulletContainer9.isGone = args.isCovPassCheck
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.checkContextRepository.showSunsetPopup.set(false)
            findNavigator().popUntil<SunsetPopupCallback>()?.onSunsetPopupFinish()
        }
    }

    override fun onClickOutside() {
        onActionButtonClicked()
    }

    override fun onCloseButtonClicked() {
        onActionButtonClicked()
    }
}

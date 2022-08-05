/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.StringRes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.onDestroyView
import com.ensody.reactivestate.validUntil
import com.ibm.health.common.navigation.android.SheetPaneNavigation
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.triggerBackPress
import de.rki.covpass.commonapp.databinding.BottomSheetViewBinding

/** Common base bottom sheet. */
public abstract class BaseBottomSheet : BaseFragment(), SheetPaneNavigation {

    @StringRes
    public open val buttonTextRes: Int? = null
    public open val heightLayoutParams: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    protected var bottomSheetBinding: BottomSheetViewBinding by validUntil(::onDestroyView)
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        bottomSheetBinding = BottomSheetViewBinding.inflate(inflater, container, false)
        // Add the bottom sheet content dynamically into the container.
        // To support view bindings, we must call super.onCreateView, as view inflation
        // is hooked in [BaseHookedFragment].
        // If the super implementation returns a non-null view, it is added as a child into the
        // content area.
        super.onCreateView(inflater, bottomSheetBinding.bottomSheetContent, savedInstanceState)?.let {
            bottomSheetBinding.bottomSheetContent.addView(it)
        }

        ViewCompat.setAccessibilityDelegate(
            bottomSheetBinding.bottomSheetHeader,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            }
        )

        bottomSheetBinding.bottomSheet.layoutParams.height = heightLayoutParams
        bottomSheetBinding.bottomSheetClose.setOnClickListener { onCloseButtonClicked() }
        buttonTextRes?.let { bottomSheetBinding.bottomSheetActionButton.text = getString(it) }
        bottomSheetBinding.bottomSheetActionButton.setOnClickListener {
            onActionButtonClicked()
        }

        // Adds padding to the content view equal to bottomSheetBottomView.height
        bottomSheetBinding.bottomSheetBottomView.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val bottomViewHeight = height
                    try {
                        bottomSheetBinding
                    } catch (e: IllegalStateException) {
                        return
                    }
                    bottomSheetBinding.bottomSheetContent.getChildAt(0)?.run {
                        setPadding(paddingLeft, paddingTop, paddingRight, bottomViewHeight)
                    }
                }
            })
        }

        return bottomSheetBinding.root
    }

    public open fun onCloseButtonClicked() {
        triggerBackPress()
    }

    protected abstract fun onActionButtonClicked()

    override fun onClickOutside() {
        super.onClickOutside()
        triggerBackPress()
    }

    public fun startTimer(
        durationInMilliseconds: Long = DEFAULT_DURATION_IN_MILLISECONDS,
        showTimerMillisecondsInFuture: Long = DEFAULT_SHOW_TIMER_MILLISECONDS_IN_FUTURE
    ) {
        timer = object : CountDownTimer(durationInMilliseconds, 1000L) {
            @SuppressLint("StringFormatInvalid")
            override fun onTick(p0: Long) {
                val time = (p0 / 1000).toInt()

                when {
                    p0 < 1000L -> {
                        this.onFinish()
                    }
                    p0 < showTimerMillisecondsInFuture -> {
                        bottomSheetBinding.bottomSheetCountdown.isVisible = true
                        bottomSheetBinding.bottomSheetCountdown.text = getString(R.string.result_countdown, time)
                    }
                }
            }
            override fun onFinish() {
                findNavigator().popAll()
            }
        }
        timer?.start()
    }

    private fun cancelTimer() {
        timer?.cancel()
    }

    override fun onDestroy() {
        cancelTimer()
        super.onDestroy()
    }

    private companion object {
        const val DEFAULT_DURATION_IN_MILLISECONDS = 120000L
        const val DEFAULT_SHOW_TIMER_MILLISECONDS_IN_FUTURE = 60000L
    }
}

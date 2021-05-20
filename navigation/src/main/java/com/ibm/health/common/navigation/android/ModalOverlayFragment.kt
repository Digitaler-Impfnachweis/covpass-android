/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Fragment which sole scope is to show a black shadow behind an [OverlayNavigation].
 *
 * Additionally, the fragment catches any click on it's area and forwards it
 * to the current active [OverlayNavigation] via [OverlayNavigation.onClickOutside].
 */
public class ModalOverlayFragment : Fragment(), ModalPaneNavigation {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.navigator_modal_overlay, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.navigator_modal_overlay_view).apply {
            accessibilityDelegate = null

            setOnClickListener {
                findNavigator().findFragment<OverlayNavigation>()?.onClickOutside()
            }
        }
    }
}

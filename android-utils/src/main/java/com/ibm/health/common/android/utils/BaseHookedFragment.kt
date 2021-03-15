package com.ibm.health.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.OnActivityResultHook
import com.ibm.health.common.android.utils.OnCreateHook
import com.ibm.health.common.android.utils.OnRequestPermissionsResultHook
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.annotations.Continue
import com.ibm.health.common.navigation.android.NavigatorOwner
import com.ibm.health.common.navigation.android.OnBackPressedNavigation

/** Common base class that comes with hook and navigation support. */
public abstract class BaseHookedFragment :
    Fragment(),
    OnBackPressedNavigation,
    OnCreateHook,
    OnActivityResultHook,
    OnRequestPermissionsResultHook {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateHook()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultHook(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResultHook(requestCode, permissions, grantResults)
    }

    override fun onBackPressed(): Abortable =
        (this as? NavigatorOwner)?.navigator?.onBackPressed()
            ?: Continue
}

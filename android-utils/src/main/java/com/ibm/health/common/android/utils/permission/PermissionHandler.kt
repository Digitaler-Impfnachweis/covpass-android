package com.ibm.health.common.android.utils.permission

import android.app.Activity
import com.ibm.health.common.android.utils.OnRequestPermissionsResultHook

public interface PermissionHandler : PermissionResponseHandler, OnRequestPermissionsResultHook {
    public val permissionManager: PermissionManager

    public fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    )

    override fun onRequestPermissionsResultHook(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionManager
            .getResponses(requestCode, permissions, grantResults)
            .forEach { onPermissionResponse(it) }
    }

    public fun requireActivity(): Activity

    public fun requestPermissions(permissions: Array<String>, requestCode: Int)
}

public interface PermissionResponseHandler {
    public fun onPermissionResponse(permissionResponse: PermissionResponse) {}
}

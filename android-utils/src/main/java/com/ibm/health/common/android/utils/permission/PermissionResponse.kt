package com.ibm.health.common.android.utils.permission

/**
 * Data class used for providing the request permission response to the request caller.
 *
 * @param requestCode The request code set when this permission was requested.
 * @param permission The requested permission.
 * @param isGranted true if the [permission] was granted, false otherwise.
 * @param showPermissionRationale true if a permission rationale should be shown, false otherwise. Default is true.
 */
public data class PermissionResponse(
    val requestCode: Int,
    val permission: String,
    val isGranted: Boolean,
    val showPermissionRationale: Boolean = true
)

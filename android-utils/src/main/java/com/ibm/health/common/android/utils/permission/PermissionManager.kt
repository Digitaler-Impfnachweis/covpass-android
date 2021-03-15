package com.ibm.health.common.android.utils.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

public class PermissionManager(private val handler: PermissionHandler) {

    public fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(handler.requireActivity(), permission).isGranted()

    // Usually we don't need a specific requestCode because we distinguish based on [permission], so we use a default
    public fun requestPermission(permission: String, requestCode: Int = 1052) {
        if (isPermissionGranted(permission)) {
            handler.onRequestPermissionsResult(
                requestCode, arrayOf(permission), intArrayOf(PackageManager.PERMISSION_GRANTED),
            )
        } else {
            handler.requestPermissions(arrayOf(permission), requestCode)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    public fun getResponses(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ): List<PermissionResponse> =
        permissions.zip(grantResults.map(Int::isGranted)) { permission, isGranted ->
            PermissionResponse(
                requestCode,
                permission,
                isGranted,
                ActivityCompat.shouldShowRequestPermissionRationale(
                    handler.requireActivity(),
                    permission
                ),
            )
        }

    @Suppress("unused")
    public fun getResponse(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        permission: String? = null,
        matcher: (PermissionResponse) -> Boolean = { it.permission == permission },
    ): PermissionResponse? =
        getResponses(requestCode, permissions, grantResults).firstOrNull { matcher(it) }
}

/**
 * Checks whether this [Int] represents a permission granted or not.
 *
 * @return true if this is a [PackageManager.PERMISSION_GRANTED], false otherwise.
 */
private fun Int.isGranted(): Boolean = this == PackageManager.PERMISSION_GRANTED

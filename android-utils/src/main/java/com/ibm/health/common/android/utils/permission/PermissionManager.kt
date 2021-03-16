package com.ibm.health.common.android.utils.permission

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/** Creates a [PermissionManager] on a fragment that implements [PermissionHandler]. */
@Suppress("FunctionName")
public fun <T> T.PermissionManager(): PermissionManager where T : Fragment, T: PermissionHandler =
    PermissionManager(this)

/** Creates a [PermissionManager] on an activity that implements [PermissionHandler]. */
@Suppress("FunctionName")
public fun <T> T.PermissionManager(): PermissionManager where T : FragmentActivity, T: PermissionHandler =
    PermissionManager(this)

public class PermissionManager internal constructor(private val handler: PermissionHandler) {

    init {

    }

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

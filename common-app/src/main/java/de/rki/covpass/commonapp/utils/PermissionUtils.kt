/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * @return True, if the camera permission is granted, else false.
 */
public fun isCameraPermissionGranted(context: Context): Boolean {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    return permissionCheckResult == PackageManager.PERMISSION_GRANTED
}

/**
 * @return True, if the permission to write to external storage is granted, else false.
 */
public fun isWriteExternalStoragePermissionGranted(context: Context): Boolean {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    return permissionCheckResult == PackageManager.PERMISSION_GRANTED
}

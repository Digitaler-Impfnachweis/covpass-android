/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.utils

import android.os.ParcelFileDescriptor
import java.io.File
import java.io.IOException

public fun File.toParcelFileDescriptor(): ParcelFileDescriptor {
    createNewFile()
    return ParcelFileDescriptor.open(this, ParcelFileDescriptor.MODE_READ_WRITE)
        ?: throw IOException()
}

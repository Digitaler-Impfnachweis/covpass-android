/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RevocationKidLocal::class,
        RevocationIndexLocal::class,
        RevocationByteOneLocal::class,
        RevocationByteTwoLocal::class
    ],
    version = 1
)

@TypeConverters(RevocationConverters::class)
public abstract class RevocationDatabase : RoomDatabase() {

    public abstract fun revocationKidDao(): RevocationKidDao

    public abstract fun revocationIndexDao(): RevocationIndexDao

    public abstract fun revocationByteOneDao(): RevocationByteOneDao

    public abstract fun revocationByteTwoDao(): RevocationByteTwoDao
}

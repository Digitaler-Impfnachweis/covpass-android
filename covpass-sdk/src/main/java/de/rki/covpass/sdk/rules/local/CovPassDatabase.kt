/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.covpass.sdk.rules.booster.local.BoosterDescriptionLocal
import de.rki.covpass.sdk.rules.booster.local.BoosterRuleLocal
import de.rki.covpass.sdk.rules.booster.local.BoosterRulesDao
import dgca.verifier.app.engine.data.source.local.rules.Converters

@Database(
    entities = [
        RuleIdentifierLocal::class,
        ValueSetIdentifierLocal::class,
        BoosterDescriptionLocal::class,
        BoosterRuleLocal::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
public abstract class CovPassDatabase : RoomDatabase() {

    public abstract fun ruleIdentifiersDao(): RuleIdentifiersDao

    public abstract fun valueSetIdentifiersDao(): ValueSetIdentifiersDao

    public abstract fun boosterRulesDao(): BoosterRulesDao
}

package de.rki.covpass.sdk.rules.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
public abstract class RuleIdentifiersDao {

    @Transaction
    public open suspend fun updateRuleIdentifiers(keep: Collection<String>, add: Collection<RuleIdentifierLocal>) {
        deleteAll(keep = keep)
        insertAllRuleIdentifiers(*add.toTypedArray())
    }

    @Query("SELECT * from rule_identifier")
    public abstract suspend fun getAllRuleIdentifiers(): List<RuleIdentifierLocal>

    @Insert
    public abstract suspend fun insertAllRuleIdentifiers(vararg ruleIdentifier: RuleIdentifierLocal)

    @Query("DELETE FROM rule_identifier WHERE identifier NOT IN (:keep)")
    public abstract suspend fun deleteAll(keep: Collection<String> = emptyList())

    @Query("SELECT country from rule_identifier GROUP BY country")
    public abstract suspend fun getAllCountryCodes(): List<String>
}

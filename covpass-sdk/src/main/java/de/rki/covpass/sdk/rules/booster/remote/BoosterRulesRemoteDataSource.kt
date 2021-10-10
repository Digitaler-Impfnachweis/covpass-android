/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster.remote

import de.rki.covpass.sdk.cert.BoosterRulesService

public class BoosterRulesRemoteDataSource(
    private val boosterRulesService: BoosterRulesService
) {

    public suspend fun getBoosterRuleIdentifiers(): List<BoosterRuleIdentifierRemote> {
        return boosterRulesService.getBoosterRuleIdentifiers()
    }

    public suspend fun getBoosterRule(hash: String): BoosterRuleRemote {
        return boosterRulesService.getBoosterRule(hash)
    }
}

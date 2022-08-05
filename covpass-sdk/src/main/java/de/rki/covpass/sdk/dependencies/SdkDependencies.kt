/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.dependencies

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ensody.reactivestate.DependencyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.covpass.http.httpConfig
import de.rki.covpass.http.pinPublicKey
import de.rki.covpass.http.util.HostPatternWhitelist
import de.rki.covpass.http.util.getDnsSubjectAlternativeNames
import de.rki.covpass.sdk.R
import de.rki.covpass.sdk.cert.BoosterCertLogicEngine
import de.rki.covpass.sdk.cert.BoosterRulesRemoteDataSource
import de.rki.covpass.sdk.cert.BoosterRulesValidator
import de.rki.covpass.sdk.cert.CertValidator
import de.rki.covpass.sdk.cert.CovPassCountriesRemoteDataSource
import de.rki.covpass.sdk.cert.CovPassDomesticRulesRemoteDataSource
import de.rki.covpass.sdk.cert.CovPassRulesRemoteDataSource
import de.rki.covpass.sdk.cert.CovPassRulesValidator
import de.rki.covpass.sdk.cert.CovPassValueSetsRemoteDataSource
import de.rki.covpass.sdk.cert.DscListDecoder
import de.rki.covpass.sdk.cert.DscListService
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.CertificateListMapper
import de.rki.covpass.sdk.cert.models.DscList
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.crypto.readPemAsset
import de.rki.covpass.sdk.crypto.readPemKeyAsset
import de.rki.covpass.sdk.reissuing.ReissuingApiService
import de.rki.covpass.sdk.reissuing.ReissuingRepository
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import de.rki.covpass.sdk.revocation.RevocationRemoteListRepository
import de.rki.covpass.sdk.revocation.database.RevocationDatabase
import de.rki.covpass.sdk.rules.CovPassCountriesRepository
import de.rki.covpass.sdk.rules.CovPassDomesticRulesRepository
import de.rki.covpass.sdk.rules.CovPassEuRulesRepository
import de.rki.covpass.sdk.rules.CovPassRule
import de.rki.covpass.sdk.rules.CovPassValueSet
import de.rki.covpass.sdk.rules.CovPassValueSetsRepository
import de.rki.covpass.sdk.rules.booster.BoosterRule
import de.rki.covpass.sdk.rules.booster.CovPassBoosterRulesRepository
import de.rki.covpass.sdk.rules.booster.local.BoosterRulesDao
import de.rki.covpass.sdk.rules.booster.local.CovPassBoosterRulesLocalDataSource
import de.rki.covpass.sdk.rules.booster.remote.BoosterRuleInitial
import de.rki.covpass.sdk.rules.booster.remote.BoosterRuleRemote
import de.rki.covpass.sdk.rules.booster.remote.toBoosterRule
import de.rki.covpass.sdk.rules.domain.rules.CovPassGetRulesUseCase
import de.rki.covpass.sdk.rules.local.CovPassDatabase
import de.rki.covpass.sdk.rules.local.countries.CountriesDao
import de.rki.covpass.sdk.rules.local.countries.CovPassCountriesLocalDataSource
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRulesDao
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRulesLocalDataSource
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRulesDao
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRulesLocalDataSource
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsDao
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsLocalDataSource
import de.rki.covpass.sdk.rules.remote.rules.eu.CovPassRuleInitial
import de.rki.covpass.sdk.rules.remote.rules.eu.CovPassRuleRemote
import de.rki.covpass.sdk.rules.remote.rules.eu.toCovPassRule
import de.rki.covpass.sdk.rules.remote.valuesets.CovPassValueSetInitial
import de.rki.covpass.sdk.rules.remote.valuesets.CovPassValueSetRemote
import de.rki.covpass.sdk.rules.remote.valuesets.toCovPassValueSet
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.ticketing.AccessTokenRepository
import de.rki.covpass.sdk.ticketing.CancellationRepository
import de.rki.covpass.sdk.ticketing.IdentityDocumentRepository
import de.rki.covpass.sdk.ticketing.TicketingApiService
import de.rki.covpass.sdk.ticketing.TicketingValidationRepository
import de.rki.covpass.sdk.ticketing.ValidationServiceIdentityRepository
import de.rki.covpass.sdk.ticketing.encoding.TicketingDgcCryptor
import de.rki.covpass.sdk.ticketing.encoding.TicketingDgcSigner
import de.rki.covpass.sdk.ticketing.encoding.TicketingValidationRequestProvider
import de.rki.covpass.sdk.utils.DscListUpdater
import de.rki.covpass.sdk.utils.RevocationCodeEncryptor
import de.rki.covpass.sdk.utils.readTextAsset
import dgca.verifier.app.engine.AffectedFieldsDataRetriever
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import dgca.verifier.app.engine.JsonLogicValidator
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.PublicKey
import java.security.cert.X509Certificate

/**
 * Global var for making the [SdkDependencies] accessible.
 */
private lateinit var _sdkDeps: SdkDependencies

@DependencyAccessor
public var sdkDeps: SdkDependencies
    get() = _sdkDeps
    set(value) {
        _sdkDeps = value
        value.init()
    }

@OptIn(DependencyAccessor::class)
public val LifecycleOwner.sdkDeps: SdkDependencies
    get() = de.rki.covpass.sdk.dependencies.sdkDeps

/**
 * Access to various dependencies for covpass-sdk module.
 */
public abstract class SdkDependencies {

    public abstract val application: Application

    public open val trustServiceHost: String by lazy {
        application.getString(R.string.trust_service_host).takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("You have to set @string/trust_service_host or override trustServiceHost")
    }

    public open val revocationListServiceHost: String by lazy {
        application.getString(R.string.revocation_list_service_host).takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException(
                "You have to set @string/revocation_list_service_host or override trustServiceHost",
            )
    }

    private val httpClient by lazy { httpConfig.ktorClient() }

    public open val backendCa: List<X509Certificate> by lazy {
        application.readPemAsset("covpass-sdk/backend-ca.pem")
    }

    public val vaasCa: List<X509Certificate> by lazy {
        application.readPemAsset("covpass-sdk/vaas-ca.pem")
    }

    public val vaasIntermediateCa: Map<String, List<X509Certificate>> by lazy {
        mapOf(
            "**.dcc-validation.eu" to application.readPemAsset(
                "covpass-sdk/vaas-tsi-ca.pem",
            ),
        )
    }

    public val vaasWhitelist: Collection<String> by lazy {
        (vaasCa.flatMap { it.getDnsSubjectAlternativeNames() } + vaasIntermediateCa.keys)
    }

    public val hostPatternWhitelist: HostPatternWhitelist by lazy {
        HostPatternWhitelist(vaasWhitelist)
    }

    public val dscList: DscList by lazy {
        decoder.decodeDscList(
            application.readTextAsset("covpass-sdk/dsc-list.json"),
        )
    }

    public val dscListService: DscListService by lazy {
        DscListService(httpClient, trustServiceHost, decoder)
    }

    public val dscRepository: DscRepository by lazy {
        DscRepository(CborSharedPrefsStore("dsc_cert_prefs", cbor), dscList)
    }

    public val dscListUpdater: DscListUpdater by lazy {
        DscListUpdater(dscListService, dscRepository, validator)
    }

    public val rulesUpdateRepository: RulesUpdateRepository by lazy {
        RulesUpdateRepository(CborSharedPrefsStore("rules_update_prefs", cbor))
    }

    public val revocationRemoteListRepository: RevocationRemoteListRepository by lazy {
        RevocationRemoteListRepository(
            httpClient,
            revocationListServiceHost,
            CborSharedPrefsStore("revocation_list_prefs", cbor),
            application.cacheDir,
            revocationListPublicKey,
            revocationLocalListRepository,
        )
    }

    public val revocationLocalListRepository: RevocationLocalListRepository by lazy {
        RevocationLocalListRepository(
            httpClient,
            revocationListServiceHost,
            CborSharedPrefsStore("revocation_list_prefs", cbor),
            revocationDatabase,
            revocationListPublicKey,
        )
    }

    public val revocationDatabase: RevocationDatabase by lazy { createDb("revocation-database") }

    public val revocationListPublicKey: PublicKey by lazy {
        application.readPemKeyAsset("covpass-sdk/revocation-list-public-key.pem").first()
    }

    public val validator: CertValidator by lazy { CertValidator(dscList.toTrustedCerts(), cbor) }

    public val decoder: DscListDecoder by lazy { DscListDecoder(publicKey.first()) }

    private val publicKey by lazy { application.readPemKeyAsset("covpass-sdk/dsc-list-signing-key.pem") }

    private val revocationPublicKey by lazy {
        application.readPemKeyAsset("covpass-sdk/revocation-public-key.pem")
    }

    public val revocationCodeEncryptor: RevocationCodeEncryptor by lazy {
        RevocationCodeEncryptor(revocationPublicKey.first())
    }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }

    public val cbor: Cbor = defaultCbor

    public val json: Json = defaultJson

    internal fun init() {
        httpConfig.pinPublicKey(backendCa + vaasCa)
        vaasIntermediateCa.forEach {
            httpConfig.pinPublicKey(it.key, it.value)
        }
    }

    public val certificateListMapper: CertificateListMapper by lazy {
        CertificateListMapper(qrCoder)
    }

    private val certLogicDeps: CertLogicDeps by lazy {
        CertLogicDeps(application)
    }

    private val dccRulesHost: String by lazy { "distribution.dcc-rules.de" }

    private val dccBoosterRulesHost: String by lazy {
        application.getString(R.string.dcc_booster_rules_host).takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException(
                "You have to set @string/dcc_booster_rules_host or override dccBoosterRulesHost",
            )
    }

    private val covPassEuRulesRemoteDataSource: CovPassRulesRemoteDataSource by lazy {
        CovPassRulesRemoteDataSource(httpClient, dccRulesHost, "rules")
    }

    private val covPassDomesticRulesRemoteDataSource: CovPassDomesticRulesRemoteDataSource by lazy {
        CovPassDomesticRulesRemoteDataSource(httpClient, dccRulesHost)
    }

    private val covPassValueSetsRemoteDataSource: CovPassValueSetsRemoteDataSource by lazy {
        CovPassValueSetsRemoteDataSource(httpClient, dccRulesHost)
    }

    private val boosterRulesRemoteDataSource: BoosterRulesRemoteDataSource by lazy {
        BoosterRulesRemoteDataSource(httpClient, dccBoosterRulesHost)
    }

    private val countriesRemoteDataSource: CovPassCountriesRemoteDataSource by lazy {
        CovPassCountriesRemoteDataSource(httpClient, dccRulesHost)
    }

    private val covPassDatabase: CovPassDatabase by lazy { createDb("covpass-database") }

    private inline fun <reified T : RoomDatabase> createDb(name: String): T =
        Room.databaseBuilder(application, T::class.java, name)
            .fallbackToDestructiveMigration()
            .build()

    private val covPassEuRulesLocalDataSource: CovPassEuRulesLocalDataSource by lazy {
        CovPassEuRulesLocalDataSource(covPassEuRulesDao)
    }

    private val covPassDomesticRulesLocalDataSource: CovPassDomesticRulesLocalDataSource by lazy {
        CovPassDomesticRulesLocalDataSource(covPassDomesticRulesDao)
    }

    private val covPassValueSetsLocalDataSource: CovPassValueSetsLocalDataSource by lazy {
        CovPassValueSetsLocalDataSource(covPassValueSetsDao)
    }

    private val covPassBoosterRulesLocalDataSource: CovPassBoosterRulesLocalDataSource by lazy {
        CovPassBoosterRulesLocalDataSource(boosterRuleDao)
    }

    private val covPassCountriesLocalDataSource: CovPassCountriesLocalDataSource by lazy {
        CovPassCountriesLocalDataSource(countriesDao)
    }

    private val covPassEuRulesDao: CovPassEuRulesDao by lazy { covPassDatabase.covPassEuRulesDao() }

    private val covPassDomesticRulesDao: CovPassDomesticRulesDao by lazy {
        covPassDatabase.covPassDomesticRulesDao()
    }

    private val covPassValueSetsDao: CovPassValueSetsDao by lazy { covPassDatabase.covPassValueSetsDao() }

    private val boosterRuleDao: BoosterRulesDao by lazy { covPassDatabase.boosterRulesDao() }

    private val countriesDao: CountriesDao by lazy { covPassDatabase.countriesDao() }

    private val euRulePath: String by lazy { "covpass-sdk/eu-rules.json" }
    private val covPassEuRulesInitial: List<CovPassRuleInitial> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(euRulePath),
        )
    }

    private val covPassEuRulesRemote: List<CovPassRuleRemote> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(euRulePath),
        )
    }

    public val bundledEuRules: List<CovPassRule> by lazy {
        covPassEuRulesRemote.zip(covPassEuRulesInitial).map {
            it.first.toCovPassRule(it.second.hash)
        }
    }

    private val domesticRulePath: String by lazy { "covpass-sdk/domestic-rules.json" }
    private val covPassDomesticRulesInitial: List<CovPassRuleInitial> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(domesticRulePath),
        )
    }

    private val covPassDomesticRulesRemote: List<CovPassRuleRemote> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(domesticRulePath),
        )
    }

    public val bundledDomesticRules: List<CovPassRule> by lazy {
        covPassDomesticRulesRemote.zip(covPassDomesticRulesInitial).map {
            it.first.toCovPassRule(it.second.hash)
        }
    }

    private val euValueSetsPath: String by lazy { "covpass-sdk/eu-value-sets.json" }
    private val covPassValueSetsRemote: List<CovPassValueSetRemote> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(euValueSetsPath),
        )
    }

    private val covPassValueSetsInitial: List<CovPassValueSetInitial> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(euValueSetsPath),
        )
    }

    public val bundledValueSets: List<CovPassValueSet> by lazy {
        covPassValueSetsRemote.zip(covPassValueSetsInitial).map {
            it.first.toCovPassValueSet(it.second.hash)
        }
    }

    private val boosterRulesPath: String by lazy { "covpass-sdk/eu-booster-rules.json" }
    public val boosterRulesRemote: List<BoosterRuleRemote> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(boosterRulesPath),
        )
    }

    public val boosterRulesInitial: List<BoosterRuleInitial> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset(boosterRulesPath),
        )
    }

    public val bundledBoosterRules: List<BoosterRule> by lazy {
        boosterRulesRemote.zip(boosterRulesInitial).map {
            it.first.toBoosterRule(it.second.hash)
        }
    }

    public val bundledCountries: List<String> by lazy {
        defaultJson.decodeFromString(
            application.readTextAsset("covpass-sdk/eu-countries.json"),
        )
    }

    public val covPassEuRulesRepository: CovPassEuRulesRepository by lazy {
        CovPassEuRulesRepository(
            covPassEuRulesRemoteDataSource,
            covPassEuRulesLocalDataSource,
            rulesUpdateRepository,
        )
    }

    public val covPassDomesticRulesRepository: CovPassDomesticRulesRepository by lazy {
        CovPassDomesticRulesRepository(
            covPassDomesticRulesRemoteDataSource,
            covPassDomesticRulesLocalDataSource,
            rulesUpdateRepository,
        )
    }

    public val covPassValueSetsRepository: CovPassValueSetsRepository by lazy {
        CovPassValueSetsRepository(
            covPassValueSetsRemoteDataSource,
            covPassValueSetsLocalDataSource,
            rulesUpdateRepository,
        )
    }

    public val covPassBoosterRulesRepository: CovPassBoosterRulesRepository by lazy {
        CovPassBoosterRulesRepository(
            boosterRulesRemoteDataSource,
            covPassBoosterRulesLocalDataSource,
            rulesUpdateRepository,
        )
    }

    public val covPassCountriesRepository: CovPassCountriesRepository by lazy {
        CovPassCountriesRepository(
            countriesRemoteDataSource,
            covPassCountriesLocalDataSource,
            rulesUpdateRepository,
        )
    }

    private val getEuRulesUseCase: CovPassGetRulesUseCase by lazy {
        CovPassGetRulesUseCase(covPassEuRulesRepository)
    }

    private val getDomesticRulesUseCase: CovPassGetRulesUseCase by lazy {
        CovPassGetRulesUseCase(covPassDomesticRulesRepository)
    }

    public val euRulesValidator: CovPassRulesValidator by lazy {
        CovPassRulesValidator(
            getEuRulesUseCase,
            certLogicDeps.certLogicEngine,
            covPassValueSetsRepository,
        )
    }

    public val domesticRulesValidator: CovPassRulesValidator by lazy {
        CovPassRulesValidator(
            getDomesticRulesUseCase,
            certLogicDeps.certLogicEngine,
            covPassValueSetsRepository,
        )
    }

    private val boosterCertLogicEngine: BoosterCertLogicEngine by lazy {
        BoosterCertLogicEngine(certLogicDeps.jsonLogicValidator)
    }

    public val boosterRulesValidator: BoosterRulesValidator by lazy {
        BoosterRulesValidator(
            boosterCertLogicEngine,
            covPassBoosterRulesRepository,
        )
    }

    public val ticketingApiService: TicketingApiService by lazy {
        TicketingApiService(httpClient)
    }

    public val identityDocumentRepository: IdentityDocumentRepository by lazy {
        IdentityDocumentRepository(ticketingApiService)
    }

    public val accessTokenRepository: AccessTokenRepository by lazy {
        AccessTokenRepository(ticketingApiService)
    }

    public val validationServiceIdentityRepository: ValidationServiceIdentityRepository by lazy {
        ValidationServiceIdentityRepository(ticketingApiService)
    }

    public val ticketingValidationRepository: TicketingValidationRepository by lazy {
        TicketingValidationRepository(ticketingApiService)
    }

    public val cancellationRepository: CancellationRepository by lazy {
        CancellationRepository(ticketingApiService)
    }

    public val ticketingValidationRequestProvider: TicketingValidationRequestProvider by lazy {
        TicketingValidationRequestProvider(TicketingDgcCryptor(), TicketingDgcSigner())
    }

    private val reissueServiceHost: String by lazy {
        application.getString(R.string.reissue_service_host).takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException(
                "You have to set @string/reissue_service_host or override reissueServiceHost",
            )
    }

    public val reissuingService: ReissuingApiService by lazy {
        ReissuingApiService(httpClient, reissueServiceHost)
    }

    public val reissuingRepository: ReissuingRepository by lazy {
        ReissuingRepository(reissuingService)
    }
}

public class CertLogicDeps(
    private val application: Application,
) {
    private val objectMapper: ObjectMapper by lazy {
        ObjectMapper().apply {
            findAndRegisterModules()
        }
    }

    private val jsonSchema: String by lazy {
        application.readTextAsset("covpass-sdk/json-schema-v1.json")
    }

    public val jsonLogicValidator: JsonLogicValidator by lazy {
        DefaultJsonLogicValidator()
    }

    private val affectedFieldsDataRetriever: AffectedFieldsDataRetriever by lazy {
        DefaultAffectedFieldsDataRetriever(objectMapper.readTree(jsonSchema), objectMapper)
    }

    public val certLogicEngine: CertLogicEngine by lazy {
        DefaultCertLogicEngine(affectedFieldsDataRetriever, jsonLogicValidator)
    }
}

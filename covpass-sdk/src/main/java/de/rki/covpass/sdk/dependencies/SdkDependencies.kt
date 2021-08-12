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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.covpass.http.httpConfig
import de.rki.covpass.http.pinPublicKey
import de.rki.covpass.sdk.R
import de.rki.covpass.sdk.cert.*
import de.rki.covpass.sdk.cert.models.CertificateListMapper
import de.rki.covpass.sdk.cert.models.DscList
import de.rki.covpass.sdk.crypto.readPemAsset
import de.rki.covpass.sdk.crypto.readPemKeyAsset
import de.rki.covpass.sdk.rules.DefaultCovPassRulesRepository
import de.rki.covpass.sdk.rules.DefaultCovPassValueSetsRepository
import de.rki.covpass.sdk.rules.RuleIdentifier
import de.rki.covpass.sdk.rules.ValueSetIdentifier
import de.rki.covpass.sdk.rules.domain.rules.CovPassGetRulesUseCase
import de.rki.covpass.sdk.rules.domain.rules.CovPassRulesUseCase
import de.rki.covpass.sdk.rules.local.*
import de.rki.covpass.sdk.rules.remote.toRuleIdentifiers
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.utils.HeaderInterceptor
import de.rki.covpass.sdk.utils.readTextAsset
import dgca.verifier.app.engine.*
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.ValueSet
import dgca.verifier.app.engine.data.source.local.rules.EngineDatabase
import dgca.verifier.app.engine.data.source.local.rules.RulesDao
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsDao
import dgca.verifier.app.engine.data.source.remote.rules.*
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetIdentifierRemote
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetRemote
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetsRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.valuesets.toValueSets
import dgca.verifier.app.engine.data.source.valuesets.DefaultValueSetsRemoteDataSource
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsApiService
import kotlinx.serialization.cbor.Cbor
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
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
public val LifecycleOwner.sdkDeps: SdkDependencies get() = de.rki.covpass.sdk.dependencies.sdkDeps

/**
 * Access to various dependencies for covpass-sdk module.
 */
public abstract class SdkDependencies {

    public abstract val application: Application

    public open val trustServiceHost: String by lazy {
        application.getString(R.string.trust_service_host).takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("You have to set @string/trust_service_host or override trustServiceHost")
    }

    private val httpClient by lazy { httpConfig.ktorClient() }

    public open val backendCa: List<X509Certificate> by lazy { application.readPemAsset("covpass-sdk/backend-ca.pem") }

    public val dscList: DscList by lazy {
        decoder.decodeDscList(
            application.readTextAsset("covpass-sdk/dsc-list.json")
        )
    }

    public val dscListService: DscListService by lazy { DscListService(httpClient, trustServiceHost) }

    public val dscRepository: DscRepository by lazy {
        DscRepository(CborSharedPrefsStore("dsc_cert_prefs", cbor), dscList)
    }

    public val validator: CertValidator by lazy { CertValidator(dscList.toTrustedCerts(), cbor) }

    public val decoder: DscListDecoder by lazy { DscListDecoder(publicKey.first()) }

    private val publicKey by lazy { application.readPemKeyAsset("covpass-sdk/dsc-list-signing-key.pem") }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }

    public val cbor: Cbor = defaultCbor

    internal fun init() {
        httpConfig.pinPublicKey(backendCa)
    }

    public val certificateListMapper: CertificateListMapper by lazy { CertificateListMapper(qrCoder) }

    private val certLogicDeps: CertLogicDeps by lazy {
        CertLogicDeps(application, dscRepository)
    }

    public val rulesRepository: DefaultCovPassRulesRepository by lazy {
        certLogicDeps.covPassRulesRepository
    }

    public val valueSetsRepository: DefaultCovPassValueSetsRepository by lazy {
        certLogicDeps.valueSetsRepository
    }

    public val rulesValidator: RulesValidator by lazy {
        certLogicDeps.rulesValidator
    }

    public val bundledRules: List<Rule> by lazy {
        certLogicDeps.bundledRules
    }

    public val bundledRuleIdentifiers: List<RuleIdentifier> by lazy {
        certLogicDeps.bundledRuleIdentifiers
    }

    public val bundledValueSets: List<ValueSet> by lazy {
        certLogicDeps.bundledValueSets
    }

    public val bundledValueSetIdentifiers: List<ValueSetIdentifier> by lazy {
        certLogicDeps.bundledValueSetIdentifiers
    }
}

public class CertLogicDeps(
    private val application: Application,
    private val dscRepository: DscRepository
) {
    private val objectMapper: ObjectMapper by lazy {
        ObjectMapper().apply {
            findAndRegisterModules()
        }
    }

    private val converterFactory: Converter.Factory by lazy {
        JacksonConverterFactory.create(objectMapper)
    }

    private val okHttpClient: OkHttpClient by lazy {
        httpConfig.okHttpClient.apply {
            newBuilder().addInterceptor(HeaderInterceptor()).build()
        }
    }

    private val retrofit: Retrofit by lazy {
        httpConfig.okHttpClient.apply {
            newBuilder().build()
        }
        Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .baseUrl("https://distribution.dcc-rules.de/")
            .callFactory { okHttpClient.newCall(it) }
            .build()
    }

    private val engineDatabase: EngineDatabase by lazy { createDb("engine") }

    private val covPassDatabase: CovPassDatabase by lazy { createDb("covpass-database") }

    private inline fun <reified T : RoomDatabase> createDb(name: String): T =
        Room.databaseBuilder(application, T::class.java, name)
            .fallbackToDestructiveMigration()
            .build()

    private val ruleIdentifiersDao: RuleIdentifiersDao by lazy {
        covPassDatabase.ruleIdentifiersDao()
    }

    private val covPassRulesLocalDataSource: CovPassRulesLocalDataSource by lazy {
        DefaultCovPassRulesLocalDataSource(ruleDao, ruleIdentifiersDao)
    }

    private val ruleDao: RulesDao by lazy { engineDatabase.rulesDao() }

    private val rulesApiService: RulesApiService by lazy {
        retrofit.create(RulesApiService::class.java)
    }

    private val valueSetIdentifiersDao: ValueSetIdentifiersDao by lazy {
        covPassDatabase.valueSetIdentifiersDao()
    }

    private val valueSetsDao: ValueSetsDao by lazy {
        engineDatabase.valueSetsDao()
    }

    private val valueSetsLocalDataSource: CovPassValueSetsLocalDataSource by lazy {
        DefaultCovPassValueSetsLocalDataSource(valueSetsDao, valueSetIdentifiersDao)
    }
    private val valueSetsApiService: ValueSetsApiService by lazy {
        retrofit.create(ValueSetsApiService::class.java)
    }
    private val valueSetsRemoteDataSource: ValueSetsRemoteDataSource by lazy {
        DefaultValueSetsRemoteDataSource(valueSetsApiService)
    }
    public val valueSetsRepository: DefaultCovPassValueSetsRepository by lazy {
        DefaultCovPassValueSetsRepository(valueSetsRemoteDataSource, valueSetsLocalDataSource)
    }

    private val rulesRemoteDateSource: RulesRemoteDataSource by lazy {
        DefaultRulesRemoteDataSource(rulesApiService)
    }

    public val bundledRuleIdentifiers: List<RuleIdentifier> by lazy {
        objectMapper.readValue(
            application.readTextAsset(
                "covpass-sdk/eu-rules-identifier.json"
            ),
            object : TypeReference<List<RuleIdentifierRemote>>() {}
        ).toRuleIdentifiers()
    }

    public val bundledRules: List<Rule> by lazy {
        objectMapper.readValue(
            application.readTextAsset("covpass-sdk/eu-rules.json"),
            object : TypeReference<List<RuleRemote>>() {}
        ).toRules()
    }

    public val bundledValueSetIdentifiers: List<ValueSetIdentifier> by lazy {
        objectMapper.readValue(
            application.readTextAsset(
                "covpass-sdk/eu-value-sets-identifier.json"
            ),
            object : TypeReference<List<ValueSetIdentifierRemote>>() {}
        ).toValueSetIdentifiersFromRemote()
    }

    public val bundledValueSets: List<ValueSet> by lazy {
        objectMapper.readValue(
            application.readTextAsset("covpass-sdk/eu-value-sets.json"),
            object : TypeReference<List<ValueSetRemote>>() {}
        ).toValueSets()
    }

    public val covPassRulesRepository: DefaultCovPassRulesRepository by lazy {
        DefaultCovPassRulesRepository(
            rulesRemoteDateSource,
            covPassRulesLocalDataSource,
            dscRepository
        )
    }

    private val jsonLogicValidator: JsonLogicValidator by lazy {
        DefaultJsonLogicValidator()
    }

    private val affectedFieldsDataRetriever: AffectedFieldsDataRetriever by lazy {
        DefaultAffectedFieldsDataRetriever(objectMapper.readTree(JSON_SCHEMA_V1), objectMapper)
    }

    private val certLogicEngine: CertLogicEngine by lazy {
        DefaultCertLogicEngine(affectedFieldsDataRetriever, jsonLogicValidator)
    }

    private val getRulesUseCase: CovPassRulesUseCase by lazy {
        CovPassGetRulesUseCase(covPassRulesRepository)
    }

    public val rulesValidator: RulesValidator by lazy {
        RulesValidator(
            getRulesUseCase,
            certLogicEngine,
            valueSetsRepository
        )
    }

    internal inline fun Retrofit.Builder.callFactory(
        crossinline body: (Request) -> Call
    ) = callFactory { request -> body(request) }
}

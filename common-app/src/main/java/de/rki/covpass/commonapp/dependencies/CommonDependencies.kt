/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.dependencies

import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.androidDeps
import com.lyft.kronos.AndroidClockFactory
import com.lyft.kronos.KronosClock
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.commonapp.kronostime.TimeValidationRepository
import de.rki.covpass.commonapp.storage.AcousticFeedbackRepository
import de.rki.covpass.commonapp.storage.CheckContextRepository
import de.rki.covpass.commonapp.storage.OnboardingRepository
import de.rki.covpass.commonapp.updateinfo.UpdateInfoRepository
import de.rki.covpass.commonapp.utils.SettingUpdateListBuilder
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.serialization.cbor.Cbor

/**
 * Global var for making the [CommonDependencies] accessible.
 */
@DependencyAccessor
public lateinit var commonDeps: CommonDependencies

@OptIn(DependencyAccessor::class)
public val LifecycleOwner.commonDeps: CommonDependencies
    get() = de.rki.covpass.commonapp.dependencies.commonDeps

/**
 * Access to various dependencies for common-app module.
 */
@OptIn(DependencyAccessor::class)
public abstract class CommonDependencies {

    /**
     * The [CommonErrorHandler].
     */
    public abstract val errorHandler: CommonErrorHandler

    public open val certRepository: CertRepository? = null

    private val cbor: Cbor = sdkDeps.cbor

    public val fileProviderAuthority: String get() = androidDeps.application.packageName + ".covpass.provider"

    public val onboardingRepository: OnboardingRepository = OnboardingRepository(
        CborSharedPrefsStore("onboarding_prefs", cbor),
    )

    public val acousticFeedbackRepository: AcousticFeedbackRepository = AcousticFeedbackRepository(
        CborSharedPrefsStore("acoustic_feedback_prefs", cbor),
    )

    public val updateInfoRepository: UpdateInfoRepository = UpdateInfoRepository(
        CborSharedPrefsStore("update_info_prefs", cbor),
    )

    public val checkContextRepository: CheckContextRepository = CheckContextRepository(
        CborSharedPrefsStore("covpass_check_prefs", cbor),
    )

    public val kronosClock: KronosClock by lazy {
        AndroidClockFactory.createKronosClock(
            context = sdkDeps.application.applicationContext,
            ntpHosts = listOf(DE_NTP_HOST),
        )
    }

    public val timeValidationRepository: TimeValidationRepository =
        TimeValidationRepository(kronosClock)

    public val settingsUpdateListBuilder: SettingUpdateListBuilder by lazy {
        SettingUpdateListBuilder(
            sdkDeps.dscRepository,
            sdkDeps.revocationLocalListRepository,
        )
    }

    private companion object {
        private const val DE_NTP_HOST = "1.de.pool.ntp.org"
    }
}

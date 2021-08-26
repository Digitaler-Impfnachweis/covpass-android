package de.rki.covpass.app.uielements

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateCardBinding
import de.rki.covpass.sdk.cert.models.CertValidationResult
import kotlin.properties.Delegates

public class CertificateCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {
    private val binding: CertificateCardBinding = CertificateCardBinding.inflate(LayoutInflater.from(context))

    public var header: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateHeaderTextview.text = newValue
    }

    private var status: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateStatusTextview.text = newValue
    }

    private var protectionText: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateProtectionTextview.text = newValue
    }

    private var protectionTextColor: Int by Delegates.observable(R.color.backgroundPrimary) { _, _, newValue ->
        binding.certificateProtectionTextview.setTextColor(newValue)
    }

    public var name: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateNameTextview.text = newValue
    }

    private var nameTextColor: Int by Delegates.observable(R.color.backgroundPrimary) { _, _, newValue ->
        binding.certificateNameTextview.setTextColor(newValue)
    }

    public var qrCodeImage: Bitmap? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateQrImageview.background = BitmapDrawable(resources, newValue)
    }

    private var isFavorite: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.certificateFavoriteButton.setImageResource(
            if (newValue) {
                R.drawable.star_white_fill
            } else {
                R.drawable.star_white
            }
        )
    }

    public var isFavoriteButtonVisible: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.certificateFavoriteButton.isVisible = newValue
    }

    private var cardBackground: Int by Delegates.observable(R.color.backgroundPrimary) { _, _, newValue ->
        binding.certificateCardview.setCardBackgroundColor(newValue)
    }

    private var statusImage: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateStatusImageview.setImageDrawable(newValue)
    }

    private var arrow: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateArrowImageview.setImageDrawable(newValue)
    }

    private var cardFadeout: Drawable? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.cardBottomFadeout.background = newValue
        }
    }

    public fun setOnFavoriteClickListener(onClickListener: OnClickListener) {
        binding.certificateFavoriteButton.setOnClickListener(onClickListener)
    }

    public fun setOnCardClickListener(onClickListener: OnClickListener) {
        binding.certificateCardview.setOnClickListener(onClickListener)
        binding.certificateCardviewScrollContent.setOnClickListener(onClickListener)
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun expiredOrInvalid(statusText: String) {
        status = statusText
        protectionTextColor = ContextCompat.getColor(context, R.color.onInfo)
        nameTextColor = ContextCompat.getColor(context, R.color.onInfo)
        cardBackground = ContextCompat.getColor(context, R.color.onBrandBase60)
        statusImage = ContextCompat.getDrawable(context, R.drawable.main_cert_expired)
        arrow = ContextCompat.getDrawable(context, R.drawable.arrow_right_white)
        cardFadeout = ContextCompat.getDrawable(context, R.drawable.common_gradient_card_fadeout_gray)
        binding.certificateQrImageview.foreground =
            ContextCompat.getDrawable(context, R.drawable.expired_overlay_icon_foreground)
        binding.certificateQrImageview.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.expired_overlay_tint))
        binding.certificateQrImageview.backgroundTintMode = PorterDuff.Mode.MULTIPLY
    }

    public fun vaccinationFullProtectionCard(
        header: String,
        status: String,
        protectionText: String,
        name: String,
        isFavorite: Boolean = false,
        certStatus: CertValidationResult = CertValidationResult.Valid
    ) {
        this.header = header
        this.protectionText = protectionText
        this.name = name
        this.isFavorite = isFavorite

        when (certStatus) {
            CertValidationResult.Valid,
            CertValidationResult.ExpiryPeriod -> {
                this.status = status
                protectionTextColor = ContextCompat.getColor(context, R.color.onInfo)
                nameTextColor = ContextCompat.getColor(context, R.color.onInfo)
                cardBackground = ContextCompat.getColor(context, R.color.info70)
                statusImage = ContextCompat.getDrawable(
                    context,
                    if (certStatus == CertValidationResult.Valid) {
                        R.drawable.main_cert_status_complete
                    } else {
                        R.drawable.main_cert_expiry_period
                    }
                )
                arrow = ContextCompat.getDrawable(context, R.drawable.arrow_right_white)
                cardFadeout = ContextCompat.getDrawable(context, R.drawable.common_gradient_card_fadeout_blue)
            }
            CertValidationResult.Invalid -> expiredOrInvalid(getString(R.string.certificates_overview_invalid_title))
            CertValidationResult.Expired -> expiredOrInvalid(getString(R.string.certificates_overview_expired_title))
        }
    }

    public fun createVaccinationCompleteOrPartialCard(
        header: String,
        status: String,
        protectionText: String,
        name: String,
        isFavorite: Boolean = false,
        certStatus: CertValidationResult = CertValidationResult.Valid
    ) {
        this.header = header
        this.protectionText = protectionText
        this.name = name
        this.isFavorite = isFavorite

        when (certStatus) {
            CertValidationResult.Valid,
            CertValidationResult.ExpiryPeriod -> {
                this.status = status
                protectionTextColor = ContextCompat.getColor(context, R.color.onBackground)
                nameTextColor = ContextCompat.getColor(context, R.color.onBackground)
                cardBackground = ContextCompat.getColor(context, R.color.info20)
                statusImage = ContextCompat.getDrawable(
                    context,
                    if (certStatus == CertValidationResult.Valid) {
                        R.drawable.main_cert_status_incomplete
                    } else {
                        R.drawable.main_cert_expiry_period
                    }
                )
                arrow = ContextCompat.getDrawable(context, R.drawable.arrow_right_blue)
                cardFadeout = ContextCompat.getDrawable(context, R.drawable.common_gradient_card_fadeout_light_blue)
            }
            CertValidationResult.Invalid -> expiredOrInvalid(getString(R.string.certificates_overview_invalid_title))
            CertValidationResult.Expired -> expiredOrInvalid(getString(R.string.certificates_overview_expired_title))
        }
    }

    public fun createTestCard(
        header: String,
        status: String,
        protectionText: String,
        name: String,
        isFavorite: Boolean = false,
        certStatus: CertValidationResult = CertValidationResult.Valid
    ) {
        this.header = header
        this.protectionText = protectionText
        this.name = name
        this.isFavorite = isFavorite

        when (certStatus) {
            CertValidationResult.Valid,
            CertValidationResult.ExpiryPeriod -> {
                this.status = status
                protectionTextColor = ContextCompat.getColor(context, R.color.onInfo)
                nameTextColor = ContextCompat.getColor(context, R.color.onInfo)
                cardBackground = ContextCompat.getColor(context, R.color.test_certificate_background)
                statusImage = ContextCompat.getDrawable(
                    context,
                    if (certStatus == CertValidationResult.Valid) {
                        R.drawable.main_cert_test
                    } else {
                        R.drawable.main_cert_expiry_period
                    }
                )
                arrow = ContextCompat.getDrawable(context, R.drawable.arrow_right_white)
                cardFadeout = ContextCompat.getDrawable(context, R.drawable.common_gradient_card_fadeout_purple)
            }
            CertValidationResult.Invalid -> expiredOrInvalid(getString(R.string.certificates_overview_invalid_title))
            CertValidationResult.Expired -> expiredOrInvalid(getString(R.string.certificates_overview_expired_title))
        }
    }

    public fun createRecoveryCard(
        header: String,
        status: String,
        protectionText: String,
        name: String,
        isFavorite: Boolean = false,
        certStatus: CertValidationResult = CertValidationResult.Valid
    ) {
        this.header = header
        this.protectionText = protectionText
        this.name = name
        this.isFavorite = isFavorite

        when (certStatus) {
            CertValidationResult.Valid,
            CertValidationResult.ExpiryPeriod -> {
                this.status = status
                protectionTextColor = ContextCompat.getColor(context, R.color.onInfo)
                nameTextColor = ContextCompat.getColor(context, R.color.onInfo)
                cardBackground = ContextCompat.getColor(context, R.color.brandAccent90)
                statusImage = ContextCompat.getDrawable(
                    context,
                    if (certStatus == CertValidationResult.Valid) {
                        R.drawable.main_cert_recovery
                    } else {
                        R.drawable.main_cert_expiry_period
                    }
                )
                arrow = ContextCompat.getDrawable(context, R.drawable.arrow_right_white)
                cardFadeout = ContextCompat.getDrawable(context, R.drawable.common_gradient_card_fadeout_dark_blue)
            }
            CertValidationResult.Invalid -> expiredOrInvalid(getString(R.string.certificates_overview_invalid_title))
            CertValidationResult.Expired -> expiredOrInvalid(getString(R.string.certificates_overview_expired_title))
        }
    }
}

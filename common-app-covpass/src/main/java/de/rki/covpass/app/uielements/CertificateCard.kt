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
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
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
    private val binding: CertificateCardBinding =
        CertificateCardBinding.inflate(LayoutInflater.from(context))

    private var status: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateStatusTextview.text = newValue
        binding.certificateStatusTextview.isVisible = newValue != null
    }

    private var cardBackground: Int by Delegates.observable(R.color.info70) { _, _, newValue ->
        binding.certificateCardview.setCardBackgroundColor(newValue)
    }

    private var statusImage: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateStatusImageview.setImageDrawable(newValue)
    }

    public var qrCodeImage: Bitmap? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateQrImageview.background = BitmapDrawable(resources, newValue)
    }

    public var isFavoriteButtonVisible: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.certificateFavoriteButton.isVisible = newValue
    }

    public fun setOnFavoriteClickListener(onClickListener: OnClickListener) {
        binding.certificateFavoriteButton.setOnClickListener(onClickListener)
    }

    public fun setOnCardClickListener(onClickListener: OnClickListener) {
        binding.certificateCardview.setOnClickListener(onClickListener)
        binding.certificateCardviewScrollContent.setOnClickListener(onClickListener)
    }

    public fun setOnCertificateStatusClickListener(onClickListener: OnClickListener) {
        binding.certificateStatusContainer.setOnClickListener(onClickListener)
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun createCertificateCardView(
        fullName: String,
        isFavorite: Boolean,
        certStatus: CertValidationResult,
        header: String,
        subtitle: String,
        @DrawableRes imageRes: Int
    ) {
        binding.certificateNameTextview.text = fullName
        binding.certificateHeaderTextview.text = header
        binding.certificateStatusTextview.text = subtitle
        validateFavoriteFlag(isFavorite)

        when (certStatus) {
            CertValidationResult.Valid,
            CertValidationResult.ExpiryPeriod -> {
                validOrExpiryPeriod(imageRes)
            }
            CertValidationResult.Invalid, CertValidationResult.Revoked ->
                expiredOrInvalid(getString(R.string.certificates_start_screen_qrcode_certificate_invalid_subtitle))
            CertValidationResult.Expired ->
                expiredOrInvalid(getString(R.string.certificates_start_screen_qrcode_certificate_expired_subtitle))
        }
    }

    private fun validateFavoriteFlag(isFavorite: Boolean) {
        binding.certificateFavoriteButton.setImageResource(
            if (isFavorite) R.drawable.star_white_fill
            else R.drawable.star_white
        )
        binding.certificateFavoriteButton.contentDescription = if (isFavorite) {
            getString(R.string.accessibility_certificate_favorite_button_label_active)
        } else {
            getString(R.string.accessibility_certificate_favorite_button_label_not_active)
        }
    }

    private fun validOrExpiryPeriod(@DrawableRes imageRes: Int) {
        cardBackground = ContextCompat.getColor(context, R.color.info70)
        statusImage = ContextCompat.getDrawable(context, imageRes)
        binding.certificateHeaderTextview.setTextColor(
            ContextCompat.getColor(context, R.color.info70)
        )
        binding.certificateStatusTextview.setTextColor(
            ContextCompat.getColor(context, R.color.info40)
        )
        binding.certificateQrImageview.foreground = null
        binding.certificateQrImageview.backgroundTintList = null
        binding.certificateQrImageview.backgroundTintMode = null
    }

    private fun expiredOrInvalid(statusText: String) {
        binding.certificateHeaderTextview.text = getString(R.string.startscreen_card_title)
        binding.certificateCovpassCheckTextview.isInvisible = true
        status = statusText
        cardBackground = ContextCompat.getColor(context, R.color.onBrandBase60)
        statusImage = ContextCompat.getDrawable(context, R.drawable.main_cert_expired)
        binding.certificateHeaderTextview.setTextColor(
            ContextCompat.getColor(context, R.color.onBrandBase60)
        )
        binding.certificateStatusTextview.setTextColor(
            ContextCompat.getColor(context, R.color.onBrandBase40)
        )
        binding.certificateQrImageview.foreground =
            ContextCompat.getDrawable(context, R.drawable.expired_overlay_icon_foreground)
        binding.certificateQrImageview.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.expired_overlay_tint))
        binding.certificateQrImageview.backgroundTintMode = PorterDuff.Mode.MULTIPLY
    }
}

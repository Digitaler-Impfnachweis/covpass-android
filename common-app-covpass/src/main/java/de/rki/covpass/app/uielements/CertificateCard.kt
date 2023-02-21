package de.rki.covpass.app.uielements

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateCardBinding
import de.rki.covpass.sdk.cert.models.CertValidationResult
import kotlin.properties.Delegates

public class CertificateCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: CertificateCardBinding =
        CertificateCardBinding.inflate(LayoutInflater.from(context))

    private var showNotification: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.certificateRedDotNotification.isVisible = newValue
        binding.certificateNotificationText.isVisible = newValue
    }

    private var cardBackground: Int by Delegates.observable(R.color.info70) { _, _, newValue ->
        binding.certificateCardview.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                newValue,
            ),
        )
    }

    public var qrCodeImage: Bitmap? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateQrImageview.background = BitmapDrawable(resources, newValue)
    }

    public fun setOnCardClickListener(onClickListener: OnClickListener) {
        binding.certificateCardview.setOnClickListener(onClickListener)
        binding.certificateCardviewScrollContent.setOnClickListener(onClickListener)
        binding.certificateArrowImageview.setOnClickListener(onClickListener)
    }

    public fun setOnCertificateStatusClickListener(onClickListener: OnClickListener) {
        binding.certificateCardview.setOnClickListener(onClickListener)
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.isFocusable = false
        binding.root.isFocusable = false
    }

    public fun createCertificateCardView(
        fullName: String,
        certStatus: CertValidationResult,
        hasNotification: Boolean,
        notificationText: String?,
    ) {
        binding.certificateNameTextview.text = fullName
        showNotification = hasNotification
        if (hasNotification) {
            binding.certificateNotificationText.text = notificationText ?: ""
        }
        when (certStatus) {
            CertValidationResult.Expired,
            CertValidationResult.Invalid,
            CertValidationResult.Revoked,
            -> {
                showInvalidCard()
            }
            else -> {}
        }
    }

    private fun showInvalidCard() {
        cardBackground = R.color.onBrandBase60
        binding.certificateQrImageview.foreground =
            ContextCompat.getDrawable(context, R.drawable.expired_overlay_icon_foreground)
        binding.certificateQrImageview.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.expired_overlay_tint))
        binding.certificateQrImageview.backgroundTintMode = PorterDuff.Mode.MULTIPLY
        // view height set to zero instead of hidden to prevent issues coming from the qr code having DimensionRatio 1:1
        binding.certificateCovpassCheckTextview.height = 0
    }
}

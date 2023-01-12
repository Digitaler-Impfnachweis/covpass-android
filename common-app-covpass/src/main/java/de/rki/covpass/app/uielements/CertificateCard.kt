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
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateCardBinding
import de.rki.covpass.sdk.cert.models.MaskStatus
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

    private var maskStatusString: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateMaskStatusTextview.text = newValue
        binding.certificateMaskStatusTextview.isVisible = newValue != null
    }
    private var maskStatusIcon: Int by Delegates.observable(R.drawable.status_mask_required) { _, _, newValue ->
        binding.certificateMaskStatusImageview.setImageResource(newValue)
    }

    private var showNotification: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.certificateRedDotNotification.isVisible = newValue
        binding.certificateNotificationText.isVisible = newValue
    }

    private var cardBackground: Int by Delegates.observable(R.color.info70) { _, _, newValue ->
        binding.certificateCardview.setCardBackgroundColor(ContextCompat.getColor(context, newValue))
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
        maskStatus: MaskStatus,
        hasNotification: Boolean,
        notificationText: String?,
        federalState: String,
    ) {
        binding.certificateNameTextview.text = fullName
        showNotification = hasNotification
        if (hasNotification) {
            binding.certificateNotificationText.text = notificationText ?: ""
        }
        showMaskStatus(maskStatus)
        updateBackground(maskStatus)
        if (maskStatus == MaskStatus.Invalid) {
            showInvalidCard()
        }
        binding.certificateFederalStateTextview.text = federalState
    }

    private fun showMaskStatus(maskRequired: MaskStatus) {
        when (maskRequired) {
            MaskStatus.NotRequired -> {
                maskStatusIcon = R.drawable.status_mask_not_required
                maskStatusString = getString(R.string.infschg_start_mask_optional)
            }
            MaskStatus.Required -> {
                maskStatusIcon = R.drawable.status_mask_required
                maskStatusString = getString(R.string.infschg_start_mask_mandatory)
            }
            MaskStatus.Invalid -> {
                maskStatusIcon = R.drawable.status_mask_invalid
                maskStatusString = getString(R.string.infschg_start_expired_revoked)
            }
            MaskStatus.NoRules -> {
                maskStatusIcon = R.drawable.status_mask_invalid
                maskStatusString = getString(R.string.infschg_start_screen_status_grey_2)
            }
        }
    }

    private fun updateBackground(maskStatus: MaskStatus) {
        cardBackground = when (maskStatus) {
            MaskStatus.NotRequired -> R.color.full_immunization_green
            MaskStatus.Required -> R.color.info70
            MaskStatus.Invalid -> R.color.onBrandBase60
            MaskStatus.NoRules -> R.color.info70
        }
    }

    private fun showInvalidCard() {
        binding.certificateFederalStateTextview.isVisible = false
        binding.certificateQrImageview.foreground =
            ContextCompat.getDrawable(context, R.drawable.expired_overlay_icon_foreground)
        binding.certificateQrImageview.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.expired_overlay_tint))
        binding.certificateQrImageview.backgroundTintMode = PorterDuff.Mode.MULTIPLY
        // view height set to zero instead of hidden to prevent issues coming from the qr code having DimensionRatio 1:1
        binding.certificateCovpassCheckTextview.height = 0
    }
}

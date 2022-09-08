package de.rki.covpass.app.uielements

import android.content.Context
import android.graphics.Bitmap
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
import de.rki.covpass.sdk.cert.models.ImmunizationStatus
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

    private var immunizationStatusString: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateImmunizationStatusTextview.text = newValue
        binding.certificateImmunizationStatusTextview.isVisible = newValue != null
    }
    private var immunizationNotificationStatusString: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateImmunizationStatusUpdatingTextview.text = newValue
        binding.certificateImmunizationStatusUpdatingTextview.isVisible = newValue != null
    }
    private var immunizationStatusIcon: Int by Delegates.observable(R.drawable.status_mask_required) { _, _, newValue ->
        binding.certificateImmunizationStatusImageview.setImageResource(newValue)
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
    }

    public fun setOnCertificateStatusClickListener(onClickListener: OnClickListener) {
        binding.certificateCardview.setOnClickListener(onClickListener)
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun createCertificateCardView(
        fullName: String,
        immunizationStatus: ImmunizationStatus,
        maskStatus: MaskStatus,
        hasNotification: Boolean,
    ) {
        binding.certificateNameTextview.text = fullName

        showMaskStatus(maskStatus)
        showImmunizationAndNotificationStatus(immunizationStatus, hasNotification)
        updateBackground(maskStatus)
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
        }
    }

    private fun showImmunizationAndNotificationStatus(
        immunizationStatus: ImmunizationStatus,
        hasNotification: Boolean,
    ) {
        when (immunizationStatus) {
            ImmunizationStatus.Full -> {
                immunizationStatusString = getString(R.string.infschg_start_immune_complete)
                immunizationStatusIcon = if (hasNotification) {
                    R.drawable.status_immunization_full_notification
                } else {
                    R.drawable.status_immunization_full
                }
            }
            ImmunizationStatus.Partial -> {
                immunizationStatusString = getString(R.string.infschg_start_immune_incomplete)
                immunizationStatusIcon = if (hasNotification) {
                    R.drawable.status_immunization_partial_notification
                } else {
                    R.drawable.status_immunization_partial
                }
            }
            ImmunizationStatus.Invalid -> {
                immunizationStatusString = getString(R.string.infschg_start_expired_revoked)
                immunizationStatusIcon = if (hasNotification) {
                    R.drawable.status_immunization_expired_notification
                } else {
                    R.drawable.status_immunization_expired
                }
            }
        }
        immunizationNotificationStatusString = if (hasNotification) {
            getString(R.string.infschg_start_notification)
        } else {
            null
        }
    }

    private fun updateBackground(maskStatus: MaskStatus) {
        cardBackground = when (maskStatus) {
            MaskStatus.NotRequired -> R.color.full_immunization_green
            MaskStatus.Required -> R.color.info70
            MaskStatus.Invalid -> R.color.onBrandBase60
        }
    }
}

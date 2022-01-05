/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validityresult

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import de.rki.covpass.app.databinding.ResultBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.CovPassValueSetsRepository
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

public abstract class ResultFragment : BaseBottomSheet() {

    private val binding by viewBinding(ResultBinding::inflate)

    override val heightLayoutParams: Int by lazy { ViewGroup.LayoutParams.MATCH_PARENT }
    private var titleString: String? = null
    private lateinit var resultType: LocalResult
    private val certs by lazy { covpassDeps.certRepository.certs }
    protected val valueSetsRepository: CovPassValueSetsRepository by lazy { sdkDeps.covPassValueSetsRepository }
    protected abstract val certId: String
    protected abstract val subtitleString: String
    protected abstract val subtitleAccessibleDescription: String
    protected abstract val derivedValidationResults: List<DerivedValidationResult>
    protected abstract val country: Country
    protected abstract val dateTime: LocalDateTime
    protected abstract val rulesCount: Int
    protected abstract val resultNoteEn: Int
    protected abstract val resultNoteDe: Int

    public abstract fun getRowList(cert: CovCertificate): List<ResultRowData>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleString = certs.value.getCombinedCertificate(certId)?.covCertificate?.fullName
        resultType = when {
            derivedValidationResults.find { it.result == LocalResult.FAIL } != null -> {
                LocalResult.FAIL
            }
            derivedValidationResults.find { it.result == LocalResult.OPEN } != null -> {
                LocalResult.OPEN
            }
            else -> LocalResult.PASSED
        }

        startRecyclerView()
        bottomSheetBinding.bottomSheetTitle.text = titleString
        bottomSheetBinding.bottomSheetSubtitle.text = subtitleString
        bottomSheetBinding.bottomSheetSubtitle.contentDescription = subtitleAccessibleDescription
        bottomSheetBinding.bottomSheetSubtitle.isVisible = true
        bottomSheetBinding.bottomSheetActionButton.isVisible = false
    }

    private fun startRecyclerView() {
        val cert = certs.value.getCombinedCertificate(certId)?.covCertificate ?: return
        ResultAdapter(this, resultNoteEn, resultNoteDe).apply {
            updateCert(certId)
            updateHeaderWarning(resultType, country, dateTime, rulesCount)
            updateList(getRowList(cert).filterNot { it.value.isNullOrEmpty() })
            attachTo(binding.resultRecyclerView)
        }
    }

    override fun onActionButtonClicked() {}

    public data class ResultRowData(
        val title: String,
        val titleAccessibleDescription: String,
        val value: String?,
        val validationResult: List<DerivedValidationResult> = emptyList(),
        val description: String? = null,
        val valueAccessibleDescription: String? = null,
    )
}

public enum class LocalResult {
    PASSED, FAIL, OPEN
}

@Parcelize
public data class DerivedValidationResult(
    val result: LocalResult,
    val description: String,
    val affectedString: List<String>,
) : Parcelable

public fun List<DerivedValidationResult>.getResultsBy(propertyName: String): List<DerivedValidationResult> {
    return this.filter { it.affectedString.contains(propertyName) }
}

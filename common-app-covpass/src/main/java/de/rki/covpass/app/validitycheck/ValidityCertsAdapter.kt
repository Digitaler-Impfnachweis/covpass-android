/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ValidityCertificateItemBinding
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.app.validityresult.DerivedValidationResult
import de.rki.covpass.app.validityresult.LocalResult
import de.rki.covpass.app.validityresult.RecoveryResultFragmentNav
import de.rki.covpass.app.validityresult.TestResultFragmentNav
import de.rki.covpass.app.validityresult.VaccinationResultFragmentNav
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.getDescriptionLanguage
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.ValidationResult
import java.time.LocalDateTime

@SuppressLint("NotifyDataSetChanged")
public class ValidityCertsAdapter(parent: Fragment) :
    BaseRecyclerViewAdapter<ValidityCertsAdapter.ValidityCertsViewHolder>(parent) {

    private lateinit var items: List<CertsValidationResults>
    private lateinit var country: Country
    private lateinit var dateTime: LocalDateTime

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ValidityCertsViewHolder {
        return ValidityCertsViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ValidityCertsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(items: List<CertsValidationResults>) {
        this.items = items
        notifyDataSetChanged()
        this.items
    }

    public fun updateDateTime(dateTime: LocalDateTime) {
        this.dateTime = dateTime
        notifyDataSetChanged()
    }

    public fun updateCountry(country: Country) {
        this.country = country
        notifyDataSetChanged()
    }

    public inner class ValidityCertsViewHolder(parent: ViewGroup) :
        BindingViewHolder<ValidityCertificateItemBinding>(
            parent,
            ValidityCertificateItemBinding::inflate,
        ) {
        public fun bind(item: CertsValidationResults) {
            with(binding) {
                val cert = item.cert
                binding.certLayout.setOnClickListener {
                    when (cert.dgcEntry) {
                        is Vaccination -> {
                            parent.findNavigator()
                                .push(
                                    VaccinationResultFragmentNav(
                                        cert.dgcEntry.id,
                                        item.results.toTempClassList("v"),
                                        country,
                                        dateTime,
                                        item.results.size,
                                    ),
                                )
                        }
                        is TestCert -> {
                            parent.findNavigator()
                                .push(
                                    TestResultFragmentNav(
                                        cert.dgcEntry.id,
                                        item.results.toTempClassList("t"),
                                        country,
                                        dateTime,
                                        item.results.size,
                                    ),
                                )
                        }
                        is Recovery -> {
                            parent.findNavigator()
                                .push(
                                    RecoveryResultFragmentNav(
                                        cert.dgcEntry.id,
                                        item.results.toTempClassList("r"),
                                        country,
                                        dateTime,
                                        item.results.size,
                                    ),
                                )
                        }
                    }
                }
                when {
                    item.results.find { it.result == Result.FAIL } != null -> {
                        binding.certificateTypeIcon.setImageResource(R.drawable.validation_fail)
                        binding.certificateStatusLayout.setBackgroundResource(R.color.danger20)
                        binding.certificateItemValidity.setText(R.string.certificate_check_validity_result_not_valid)
                    }
                    item.results.find { it.result == Result.OPEN } != null -> {
                        if (cert.dgcEntry is TestCert) {
                            binding.certificateTypeIcon.setImageResource(R.drawable.validation_test_open)
                        } else {
                            binding.certificateTypeIcon.setImageResource(R.drawable.validation_open)
                        }
                        binding.certificateStatusLayout.setBackgroundResource(R.color.warning20)
                        binding.certificateItemValidity.setText(R.string.certificate_check_validity_result_not_testable)
                    }
                    item.results.isEmpty() -> {
                        binding.certificateTypeIcon.setImageResource(R.drawable.validation_no_rules)
                        binding.certificateStatusLayout.setBackgroundResource(R.color.warning20)
                        binding.certificateItemValidity.setText(
                            R.string.check_validity_no_rules_status,
                        )
                    }
                    else -> {
                        if (cert.dgcEntry is TestCert) {
                            binding.certificateTypeIcon.setImageResource(R.drawable.validation_test_passed)
                        } else {
                            binding.certificateTypeIcon.setImageResource(R.drawable.validation_passed)
                        }
                        binding.certificateStatusLayout.setBackgroundResource(R.color.success20)
                        binding.certificateItemValidity.setText(R.string.certificate_check_validity_result_valid)
                    }
                }
                certificateItemName.text = cert.fullName
                when (cert.dgcEntry) {
                    is Vaccination -> {
                        certificateItemSubtitle.setText(R.string.certificate_check_validity_vaccination)
                    }
                    is TestCert -> {
                        certificateItemSubtitle.setText(R.string.certificate_check_validity_test)
                    }
                    is Recovery -> {
                        certificateItemSubtitle.setText(R.string.certificate_check_validity_recovery)
                    }
                }
            }
        }
    }

    private fun Result.toTempResult(): LocalResult = when (this) {
        Result.FAIL -> LocalResult.FAIL
        Result.OPEN -> LocalResult.OPEN
        Result.PASSED -> LocalResult.PASSED
    }

    private fun List<ValidationResult>.toTempClassList(certType: String): List<DerivedValidationResult> {
        return filter { it.result == Result.FAIL || it.result == Result.OPEN }
            .map { validationResult ->
                DerivedValidationResult(
                    result = validationResult.result.toTempResult(),
                    description = validationResult.rule.getDescriptionFor(getDescriptionLanguage()),
                    affectedString = validationResult.rule.affectedString.map { it.drop("$certType.0.".length) },
                )
            }
    }
}

/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.importcertificate

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ImportCertificateResultPopupContentBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

public interface ImportCertificatesResultCallback {
    public fun importCertificateFinish()
}

@Parcelize
public class ImportCertificatesResultFragmentNav(
    public val fromShareOption: Boolean = false,
) : FragmentNav(ImportCertificatesResultFragment::class)

public class ImportCertificatesResultFragment : BaseBottomSheet() {

    private val fromShareOption: Boolean by lazy { (getArgs<ImportCertificatesResultFragmentNav>().fromShareOption) }
    private val binding by viewBinding(ImportCertificateResultPopupContentBinding::inflate)
    override val buttonTextRes: Int = R.string.file_import_success_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.importSuccessTitle.setText(R.string.file_import_success_title)
        binding.importResultInfo.setText(R.string.file_import_success_copy)
    }

    override fun onActionButtonClicked() {
        if (fromShareOption) {
            findNavigator().popUntil<ImportCertificatesResultCallback>()?.importCertificateFinish()
        } else {
            findNavigator().popAll()
        }
    }
}

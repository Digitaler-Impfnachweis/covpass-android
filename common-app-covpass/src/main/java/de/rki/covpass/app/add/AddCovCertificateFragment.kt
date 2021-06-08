/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.add

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.scanner.CovPassQRScannerFragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
internal class AddCovCertificateFragmentNav : FragmentNav(AddCovCertificateFragment::class)

/**
 * Fragment which shows the instructions for QR code scan
 */
// FIXME BVC-1370
internal class AddCovCertificateFragment : BaseBottomSheet() {

    override fun onActionButtonClicked() {
        findNavigator().pop()
        findNavigator().push(CovPassQRScannerFragmentNav())
    }
}

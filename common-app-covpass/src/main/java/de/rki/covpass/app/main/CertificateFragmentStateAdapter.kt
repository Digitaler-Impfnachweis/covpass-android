/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ibm.health.common.android.utils.BaseFragmentStateAdapter
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList

/**
 * [FragmentStateAdapter] which holds a list of [CertificateFragment]
 */
@SuppressLint("NotifyDataSetChanged")
internal class CertificateFragmentStateAdapter(parent: Fragment) : BaseFragmentStateAdapter(parent) {

    private var fragments = listOf<CertificateFragment>()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemId(position: Int): Long = getId(fragments[position])

    override fun containsItem(itemId: Long): Boolean = fragments.any { getId(it) == itemId }

    fun createFragments(certificateList: GroupedCertificatesList) {
        fragments = certificateList.getSortedCertificates().map {
            CertificateFragmentNav(it.id).build() as CertificateFragment
        }
        // TODO: Optimize this to only update what has really changed
        notifyDataSetChanged()
    }

    fun getItemPosition(certId: GroupedCertificatesId): Int {
        return fragments.indexOfFirst {
            it.args.certId == certId
        }
    }

    private fun getId(it: CertificateFragment) = it.args.certId.hashCode().toLong()
}

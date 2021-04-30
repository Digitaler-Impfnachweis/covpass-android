package com.ibm.health.vaccination.app.vaccinee.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList

class CertificateFragmentStateAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private var fragments = listOf<CertificateFragment>()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemId(position: Int): Long = getId(fragments[position])

    override fun containsItem(itemId: Long): Boolean = fragments.any { getId(it) == itemId }

    fun createFragments(certificateList: GroupedCertificatesList) {
        fragments = certificateList.getSortedCertificates().map {
            CertificateFragmentNav(it.getMainCertId()).build() as CertificateFragment
        }
        notifyDataSetChanged()
    }

    fun getItemPosition(certId: String): Int {
        return fragments.indexOfFirst {
            it.args.certId == certId
        }
    }

    fun getFragment(position: Int): CertificateFragment = fragments[position]

    private fun getId(it: CertificateFragment) = it.args.certId.hashCode().toLong()
}

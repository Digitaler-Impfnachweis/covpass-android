package com.ibm.health.vaccination.app.vaccinee.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
import java.util.ArrayList

class CertificateFragmentStateAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private val fragmentList = ArrayList<Fragment>()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    override fun getItemId(position: Int): Long = getId(fragmentList[position])

    override fun containsItem(itemId: Long): Boolean = fragmentList.any { getId(it) == itemId }

    fun createFragments(certificateList: GroupedCertificatesList) {
        fragmentList.clear()
        certificateList.getSortedCertificates().forEach {
            fragmentList.add(CertificateFragmentNav(it.getMainCertId()).build())
        }
        notifyDataSetChanged()
    }

    fun getItemPosition(certId: String): Int {
        return fragmentList.indexOfFirst {
            it.getArgs<CertificateFragmentNav>().certId == certId
        }
    }

    fun getFragment(position: Int): Fragment = fragmentList[position]

    private fun getId(it: Fragment) = it.getArgs<CertificateFragmentNav>().certId.hashCode().toLong()
}

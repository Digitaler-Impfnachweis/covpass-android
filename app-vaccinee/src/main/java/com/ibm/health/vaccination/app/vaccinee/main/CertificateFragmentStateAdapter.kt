package com.ibm.health.vaccination.app.vaccinee.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
import java.util.ArrayList

class CertificateFragmentStateAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private val fragmentList = ArrayList<Fragment>()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    fun createFragments(certificateList: GroupedCertificatesList) {
        fragmentList.clear()
        certificateList.getSortedCertificates().forEach {
            fragmentList.add(CertificateFragmentNav(it.getMainCertId()).build())
        }
        notifyDataSetChanged()
    }
}

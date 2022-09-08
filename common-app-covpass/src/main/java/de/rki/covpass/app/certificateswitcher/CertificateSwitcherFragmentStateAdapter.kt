/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.certificateswitcher

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ibm.health.common.android.utils.BaseFragmentStateAdapter
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId

/**
 * [FragmentStateAdapter] which holds a list of [CertificateSwitcherItemFragment]
 */
@SuppressLint("NotifyDataSetChanged")
internal class CertificateSwitcherFragmentStateAdapter(
    parent: Fragment,
) : BaseFragmentStateAdapter(parent) {

    private var fragments = mutableListOf<CertificateSwitcherItemFragment>()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemId(position: Int): Long = getId(fragments[position])

    override fun containsItem(itemId: Long): Boolean = fragments.any { getId(it) == itemId }

    fun getFragment(position: Int) = fragments[position]

    fun updateFragment(position: Int, fragment: CertificateSwitcherItemFragment) {
        fragments[position] = fragment
        notifyDataSetChanged()
    }

    fun createFragments(
        groupedCertificatesId: GroupedCertificatesId,
        certificateList: List<String>,
    ) {
        fragments = certificateList.map { id ->
            CertificateSwitcherItemFragmentNav(
                groupedCertificatesId,
                id,
            ).build() as CertificateSwitcherItemFragment
        }.toMutableList()

        notifyDataSetChanged()
    }

    private fun getId(it: CertificateSwitcherItemFragment) = it.args.id.hashCode().toLong()
}

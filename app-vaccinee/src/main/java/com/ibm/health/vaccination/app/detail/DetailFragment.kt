package com.ibm.health.vaccination.app.detail

import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.vaccination.app.databinding.DetailBinding
import kotlinx.parcelize.Parcelize

@Parcelize
class DetailFragmentNav : FragmentNav(DetailFragment::class)

class DetailFragment : BaseFragment() {

    private val binding by viewBinding(DetailBinding::inflate)
}

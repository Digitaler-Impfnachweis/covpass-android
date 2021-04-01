package com.ibm.health.vaccination.app.main

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.BaseFragment
import kotlinx.parcelize.Parcelize

@Parcelize
class MainFragmentNav : FragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment()

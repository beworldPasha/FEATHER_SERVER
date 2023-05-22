package com.app.feather


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AuthPagerAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {
    private val signInFragment: SignInFragment = SignInFragment()
    private val signUpFragment: SignUpFragment = SignUpFragment()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> signInFragment
            else -> signUpFragment
        }
    }

}
package com.app.feather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.feather.databinding.FragmentMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private var applicationNavController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        binding = FragmentMainBinding.bind(view)

        return view
    }

    private fun setToolbarButtonsListener() {
        applicationNavController?.apply {
            binding.settingsToolbarButton.setOnClickListener {
                navigate(R.id.action_mainFragment_to_settingsFragment)
            }
            binding.historyToolbarButton.setOnClickListener {
                navigate(R.id.action_mainFragment_to_historyFragment)
            }
            binding.searchingToolbarButton.setOnClickListener {
                navigate(R.id.action_mainFragment_to_searchingFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        applicationNavController = activity
            ?.findViewById<FragmentContainerView>(R.id.applicationNavigationFragmentContainerView)
            ?.getFragment<NavHostFragment>()?.navController

        val pagesNavController = binding
            .pagesNavigationFragmentContainerView.getFragment<NavHostFragment>().navController
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.apply {
            setupWithNavController(pagesNavController)
        }
        setToolbarButtonsListener()

        super.onViewCreated(view, savedInstanceState)
    }
}
package com.application.data1.payment.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.application.data1.payment.R
import com.x930073498.android_box.core.BaseFragment
import com.x930073498.box.core.BoxOwner

class MainFragment : BaseFragment(R.layout.fragment_main) {

    private val viewModel by viewModels<MainViewModel>()
    companion object {
        fun newInstance() = MainFragment()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
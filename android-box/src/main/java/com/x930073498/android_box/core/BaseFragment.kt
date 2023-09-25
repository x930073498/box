package com.x930073498.android_box.core

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.x930073498.box.core.BoxOwner


abstract class BaseFragment(layout: Int = 0) : Fragment(layout), BoxOwner by BoxOwner() {

    private lateinit var factory: ViewModelProvider.Factory

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            if (this::factory.isInitialized) {
                return factory
            }
            factory = WrapFactory(super.defaultViewModelProviderFactory, this)
            return factory
        }
}
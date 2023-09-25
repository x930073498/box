package com.x930073498.android_box.core

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import com.x930073498.box.core.BoxOwner

abstract class BaseActivity(layout: Int = 0) : AppCompatActivity(layout), BoxOwner by BoxOwner() {
    private lateinit var viewModelFactory: ViewModelProvider.Factory

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            if (::viewModelFactory.isInitialized) {
                return viewModelFactory
            }
            viewModelFactory = WrapFactory(super.defaultViewModelProviderFactory, this)
            return viewModelFactory
        }

}
package com.x930073498.android_box.core

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import java.lang.ref.WeakReference

class WrapFactory(
    private val factory: ViewModelProvider.Factory,
    owner: LifecycleOwner
) : ViewModelProvider.Factory {
    private val ownerRef: WeakReference<LifecycleOwner> = WeakReference(owner)
    private val _owner: LifecycleOwner?
        get() = ownerRef.get()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = factory.create(modelClass)
        if (viewModel is LifecycleObserver) {
            _owner?.lifecycle?.addObserver(viewModel)
        }
        return viewModel
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val viewModel = factory.create(modelClass, extras)
        if (viewModel is LifecycleObserver) {
            _owner?.lifecycle?.addObserver(viewModel)
        }
        return viewModel
    }
}
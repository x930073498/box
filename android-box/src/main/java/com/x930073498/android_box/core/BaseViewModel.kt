package com.x930073498.android_box.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.x930073498.box.core.BoxOwner

abstract class BaseViewModel(application: Application, val savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application), BoxOwner by BoxOwner() {
}
package com.application.data1.payment.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.x930073498.box.core.BoxOwner

class MainViewModel(application: Application, val savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application), BoxOwner by BoxOwner() {

}
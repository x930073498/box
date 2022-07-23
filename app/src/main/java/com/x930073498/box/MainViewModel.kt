package com.x930073498.box

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    application: Application,
    private val _savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), BoxProvider by BoxProvider() {
    var text by property("")
        private set


    fun setText() {
        viewModelScope.launch(Dispatchers.IO) {
            text = UUID.randomUUID().toString()
        }
    }

}
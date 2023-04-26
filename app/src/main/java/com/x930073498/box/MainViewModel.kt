package com.x930073498.box

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.x930073498.box.core.BoxOwner
import com.x930073498.box.core.initial
import com.x930073498.box.event.Event
import com.x930073498.box.event.pushEvent
import com.x930073498.box.property.property
import com.x930073498.box.property.resetProperty
import kotlinx.coroutines.*


class MainViewModel(
    application: Application,
    private val _savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), BoxOwner by BoxOwner() {
    var a by property(initializer = { null initial 0L })
    var b by property(1L)


    private var job: Job? = null

    private fun launchJob(): Job = viewModelScope.launch(Dispatchers.IO) {
        while (isActive) {
            val c = a + b
            a = b
            b = c
            delay(100)
        }
    }

    //觸發
    fun triggerJob() {
        if (job?.isActive == true) {
            job?.cancel()
            toast("当前最终值为$b")
            resetProperty()
        } else {
            job = launchJob()
        }

    }

    private fun toast(text: String) {
        EventExchange.pushEvent(ToastEvent, text)
    }


}
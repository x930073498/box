package com.x930073498.box

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.x930073498.box.databinding.ActivitySecondBinding
import com.x930073498.box.event.eventFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SecondActivity : AppCompatActivity() {
    private val binding by viewBinding<ActivitySecondBinding>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
//      lifecycleScope.launch {
//          EventExchange.eventFlow(ToastEvent)
//              .onEach {
//                  binding.tv.text=it
//              }
//              .collect()
//      }

        lifecycleScope.launch {
            var toast: Toast? = null
            EventExchange.eventFlow(ToastEvent)
                .flowWithLifecycle(lifecycle,Lifecycle.State.CREATED)
                .onEach {
                    toast?.cancel()
                    toast = Toast.makeText(applicationContext, it, Toast.LENGTH_LONG)
                    toast?.show()
                }
                .onCompletion {
                    toast?.cancel()
                }
                .collect()
        }
    }
}
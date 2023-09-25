package com.x930073498.box

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.x930073498.box.core.BoxOwner
import com.x930073498.box.databinding.ActivityMainBinding
import com.x930073498.box.event.Event
import com.x930073498.box.event.eventFlow
import com.x930073498.box.property.subscribe
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

object ToastEvent : Event<String> by Event(true, 1)
//交流
internal object EventExchange:BoxOwner by BoxOwner()
fun <T,OWNER,V>T.doOnBox(owner: OWNER,transformer:suspend OWNER.()->Flow<V>) where T:LifecycleOwner,OWNER:BoxOwner{
    lifecycleScope.launch {
        transformer.invoke(owner).collect()
    }
}

class MainActivity : AppCompatActivity(R.layout.activity_main), BoxOwner by BoxOwner() {

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding<ActivityMainBinding>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doOnBox(viewModel){
            subscribe(::a).onEach {
                binding.tvA.text = "$it"
            }
        }
        lifecycleScope.launch {
            viewModel.subscribe(viewModel::b)
                .onEach {
                    binding.tvB.text = "$it"
                }
                .collect()
        }
        lifecycleScope.launch {
            var toast: Toast? = null
            EventExchange.eventFlow(ToastEvent)
                .onEach {
                    toast?.cancel()
                    toast = Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT)
                    toast?.show()
                }
                .onCompletion {
                    toast?.cancel()
                }
                .collect()
        }
        viewModel.triggerJob()
        binding.btn.setOnClickListener {
            viewModel.triggerJob()
        }
        binding.jump.setOnClickListener {
            startActivity(Intent(this,SecondActivity::class.java))
        }
    }
}

interface CoroutineScopeProvider {
    val coroutineScope: CoroutineScope
//    fun <T:BoxProvider,V,S>T.withProperty(property:KProperty1<T,V>,
//                                          option:QueryOption=QueryOption.Standard,
//                                          flowMap:Flow<V>.()->Flow<S>)=subscribe(property)
}
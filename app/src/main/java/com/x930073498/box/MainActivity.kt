package com.x930073498.box

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.x930073498.box.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KProperty1

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding<ActivityMainBinding>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribe(MainViewModel::text, option = QueryOption.OnlySet)
            .onEach {
                binding.tv.text = it
            }
            .launchIn(lifecycleScope)
        binding.btn.setOnClickListener {
            viewModel.setText()
        }
    }
}
interface CoroutineScopeProvider{
    val coroutineScope:CoroutineScope
//    fun <T:BoxProvider,V,S>T.withProperty(property:KProperty1<T,V>,
//                                          option:QueryOption=QueryOption.Standard,
//                                          flowMap:Flow<V>.()->Flow<S>)=subscribe(property)
}
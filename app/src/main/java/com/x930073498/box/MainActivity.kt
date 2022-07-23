package com.x930073498.box

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.x930073498.box.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding<ActivityMainBinding>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.subscribe(MainViewModel::text).onEach {
            binding.tv.text = it
        }.launchIn(lifecycleScope)
        binding.btn.setOnClickListener {
            viewModel.setText()
        }
    }
}
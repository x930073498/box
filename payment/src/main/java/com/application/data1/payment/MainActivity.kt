package com.application.data1.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.application.data1.payment.ui.main.MainFragment
import com.x930073498.android_box.core.BaseActivity
import com.x930073498.box.core.BoxOwner

class MainActivity : BaseActivity(R.layout.activity_main){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}
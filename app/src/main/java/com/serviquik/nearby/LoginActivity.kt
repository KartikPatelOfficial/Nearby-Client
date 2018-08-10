package com.serviquik.nearby

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val ft = supportFragmentManager!!.beginTransaction()
        ft.replace(R.id.LoginFramLayout, SignInFragment(), "RootFragment")
        ft.commit()

    }
}

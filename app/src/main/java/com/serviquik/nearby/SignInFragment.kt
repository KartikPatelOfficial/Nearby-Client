package com.serviquik.nearby

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class SignInFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val signUpBtn: Button = view.findViewById(R.id.signInSignUpBtn)

        signUpBtn.setOnClickListener {
            val ft = fragmentManager!!.beginTransaction()
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            ft.replace(R.id.LoginFramLayout, SignUpFragment(), "NewFragment")
            ft.addToBackStack("RootFragment")
            ft.commit()

        }

        val signInBtn:Button = view.findViewById(R.id.loginSignInBtn)

        signInBtn.setOnClickListener {
            startActivity(Intent(activity,MainActivity::class.java))
            activity!!.finish()
        }

        return view
    }

}

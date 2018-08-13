package com.serviquik.nearby.auth

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.serviquik.nearby.MainActivity
import com.serviquik.nearby.R

class SignInFragment : Fragment() {

    val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val signUpBtn: Button = view.findViewById(R.id.signInSignUpBtn)
        val emailET: EditText = view.findViewById(R.id.signInEmail)
        val passwordET: EditText = view.findViewById(R.id.signInPassword)
        val signInBtn: Button = view.findViewById(R.id.loginSignInBtn)

        signUpBtn.setOnClickListener {
            val ft = fragmentManager!!.beginTransaction()
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            ft.replace(R.id.LoginFramLayout, SignUpFragment(), "NewFragment")
            ft.addToBackStack("RootFragment")
            ft.commit()

        }

        signInBtn.setOnClickListener {
            val email = emailET.text.toString()
            val password = passwordET.text.toString()
            signIn(email, password)
        }

        return view
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                startActivity(Intent(context!!, MainActivity::class.java))
                activity!!.finish()
            } else {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
            }
        }
    }

}

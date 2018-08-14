package com.serviquik.nearby.auth


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.serviquik.nearby.R

class SignUpFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        val usernameET = view.findViewById<EditText>(R.id.signUpUsername)
        val emailET = view.findViewById<EditText>(R.id.signUpEmail)
        val passwordET = view.findViewById<EditText>(R.id.signUpPassword)
        val confirmPasswordET = view.findViewById<EditText>(R.id.signUpPasswordConfirm)
        val nextBtn = view.findViewById<Button>(R.id.loginNextBtn)

        nextBtn.setOnClickListener {
            val email = emailET.text.toString()
            val password = passwordET.text.toString()
            val confirmPassword = confirmPasswordET.text.toString()
            val userName = usernameET.text.toString()

            if (password == confirmPassword) {

                val bundle = Bundle()
                bundle.putString("Email", email)
                bundle.putString("Password", password)
                bundle.putString("Username", userName)

                val fragment = SignUpDetailsFragment()
                fragment.arguments = bundle

                val ft = fragmentManager!!.beginTransaction()
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                ft.replace(R.id.LoginFramLayout, fragment, "NewFragment")
                ft.addToBackStack("RootFragment")
                ft.commit()
            }

        }

        return view
    }


}

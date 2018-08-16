package com.serviquik.nearby.auth

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.serviquik.nearby.R

class SignInFragment : Fragment() {

    val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val okButton: Button = view.findViewById(R.id.loginVerifyBtn)
        val editText: EditText = view.findViewById(R.id.signInPhoneET)

        okButton.setOnClickListener {
            val phoneNumber = editText.text.toString()

            if (phoneNumber.length != 9){
                AlertDialog.Builder(context!!).setTitle("Error").setMessage("Please enter correct mobile number.").show()
                return@setOnClickListener
            }


        }

        return view
    }

}

package com.serviquik.nearby


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

class SignUpFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        val usernameET = view.findViewById<EditText>(R.id.signUpUsername)
        val emailET = view.findViewById<EditText>(R.id.signUpEmail)
        val passwordET = view.findViewById<EditText>(R.id.signUpPassword)
        val confirmPasswordET = view.findViewById<EditText>(R.id.signUpPasswordConfirm)
        val signUpBtn = view.findViewById<Button>(R.id.loginSignUpBtn)

        signUpBtn.setOnClickListener {
            val email = emailET.text.toString()
            val password = passwordET.text.toString()
            val confirmPassword = confirmPasswordET.text.toString()
            val userName = usernameET.text.toString()

            if (password == confirmPassword) {
                signUpUser(email, password, userName)
            }

        }

        return view
    }

    private fun signUpUser(email: String, password: String, userName: String) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                //Todo update Database
                val lat = LoginActivity.lat
                val lon = LoginActivity.long

                Toast.makeText(context,"OK",Toast.LENGTH_SHORT).show()

                addDataToDatabase(userName,email,lat,lon)

            }else{
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
            }
        }
    }

    private fun addDataToDatabase(userName: String, email: String, lat: Double?, lon: Double?) {
        Log.d("---->","Username : $userName \n Email : $email \n lat : $lat \n Lon : $lon")
    }


}

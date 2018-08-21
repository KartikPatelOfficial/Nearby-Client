package com.serviquik.nearby.customer


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R


class ManageCustomerFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        var name = ""
        var number = ""
        var email = ""
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_customer, container, false)

        view.findViewById<FloatingActionButton>(R.id.manageCustomerRecyclerViewFAB).setOnClickListener { _ ->
            val inflater1 = LayoutInflater.from(context!!)
            val dialog = inflater1.inflate(R.layout.fragment_sign_in, null)
            val dialogeBuilder = AlertDialog.Builder(context!!)
            dialogeBuilder.setView(dialog)

            dialog.findViewById<TextView>(R.id.signInTitleTV).text = "Fill detail"
            val editText = dialog.findViewById<EditText>(R.id.signInPhoneET)
            editText.hint = "Name"

            val button = dialog.findViewById<Button>(R.id.loginVerifyBtn)
            button.text = "Next"
            button.setOnClickListener { _ ->
                name = editText.text.toString()
                db.collection("Vendors").document(auth.uid!!).collection("LocalClients").whereEqualTo("Name", name).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result != null) {
                            for (document in it.result) {
                                number = document.getString("Number")!!
                                email = document.getString("Email")!!
                            }
                            makeBill()
                        } else {
                            otherDialog()
                        }
                    } else {
                        otherDialog()
                    }
                }
            }

        }

        return view
    }

    private fun otherDialog() {
        val inflater = LayoutInflater.from(context!!)
        val dialog = inflater.inflate(R.layout.get_local_detail, null)
        val dialogeBuilder = AlertDialog.Builder(context!!)
        dialogeBuilder.setView(dialog)

        val numberEt = dialog.findViewById<EditText>(R.id.getLocalPhoneEt)
        val emailEt = dialog.findViewById<EditText>(R.id.getLocalEmailEt)

        dialogeBuilder.setPositiveButton("Ok") { _, _ ->
            number = numberEt.text.toString()
            email = emailEt.text.toString()
            if (checkNull(numberEt, number)) {
                return@setPositiveButton
            }
            if (checkNull(emailEt, email)) {
                return@setPositiveButton
            }
            makeBill()
        }
    }

    private fun makeBill() {
        //todo make bill
    }

    private fun checkNull(view: EditText, string: String): Boolean {
        if (TextUtils.isEmpty(string)) {
            view.error = "Please enter detail"
            return true
        }
        return false
    }

}

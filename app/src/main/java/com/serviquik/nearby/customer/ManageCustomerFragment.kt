package com.serviquik.nearby.customer


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
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
import com.serviquik.nearby.bill.BillFragment


class ManageCustomerFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val customers = ArrayList<Customer>()
    private lateinit var adapter: ManageCustomerAdapter
    private var customerSize = 0

    companion object {
        var name = ""
        var number = ""
        var email = ""
        var cid = ""
    }

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_customer, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.manageCustomerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        adapter = ManageCustomerAdapter(customers, context!!, fragmentManager!!)
        recyclerView.adapter = adapter

        progressDialog.show()

        db.collection("Vendors").document(auth.uid!!).collection("LocalClients").get().addOnCompleteListener {
            if (it.isSuccessful) {
                customerSize = it.result.size()
                for (doc in it.result) {
                    val isLocal = doc.getBoolean("isLocal")!!
                    var profileURL: String? = null
                    if (!isLocal) {
                        profileURL = doc.getString("ProfileURL")
                    }
                    val customer = Customer(doc.getString("Name")!!, doc.getString("Email")!!, doc.getString("Number")!!, doc.id, isLocal, profileURL)
                    if (customers.size < customerSize) {
                        customers.add(customer)
                    }
                }
                progressDialog.dismiss()
                adapter.notifyDataSetChanged()
            }
        }

        view.findViewById<View>(R.id.manageCustomerRecyclerViewFAB).setOnClickListener { _ ->
            val inflater1 = LayoutInflater.from(context!!)
            val dialog = inflater1.inflate(R.layout.fragment_sign_in, null)
            val dialogeBuilder = AlertDialog.Builder(context!!)
            dialogeBuilder.setView(dialog)

            dialog.findViewById<TextView>(R.id.signInTitleTV).text = "Fill detail"
            val editText = dialog.findViewById<EditText>(R.id.signInPhoneET)
            editText.hint = "Number"
            editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME

            val button = dialog.findViewById<Button>(R.id.loginVerifyBtn)
            button.visibility = View.GONE
            button.text = "Next"
            dialogeBuilder.setPositiveButton("Next") { _, _ ->
                number = editText.text.toString()
                progressDialog.show()
                db.collection("Vendors").document(auth.uid!!).collection("LocalClients").whereEqualTo("Number", number).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        progressDialog.dismiss()
                        if (it.result != null) {
                            var isExist = false
                            for (document in it.result) {
                                if (document.exists()) {
                                    isExist = true
                                    cid = document.id
                                    name = document.getString("Name")!!
                                    email = document.getString("Email")!!
                                } else {
                                    otherDialog()
                                }
                            }
                            if (isExist) {
                                makeBill()
                            } else {
                                otherDialog()
                            }
                        } else {
                            otherDialog()
                        }
                    } else {
                        progressDialog.dismiss()
                        otherDialog()
                    }
                }
            }
            dialogeBuilder.create().show()

        }

        return view
    }

    private fun otherDialog() {
        val inflater = LayoutInflater.from(context!!)
        val dialog = inflater.inflate(R.layout.get_local_detail, null)
        val dialogeBuilder = AlertDialog.Builder(context!!)
        dialogeBuilder.setView(dialog)
        dialogeBuilder.setTitle("Fill Details")

        val nameEt = dialog.findViewById<EditText>(R.id.getLocalNameEt)
        val emailEt = dialog.findViewById<EditText>(R.id.getLocalEmailEt)

        dialogeBuilder.setPositiveButton("Ok") { _, _ ->
            name = nameEt.text.toString()
            email = emailEt.text.toString()
            if (checkNull(nameEt, name)) {
                return@setPositiveButton
            }
            if (checkNull(emailEt, email)) {
                return@setPositiveButton
            }
            val data = HashMap<String, Any>()
            data["Name"] = name
            data["Number"] = number
            data["Email"] = email
            data["isLocal"] = true
            db.collection("Vendors").document(auth.uid!!).collection("LocalClients").document().set(data).addOnCompleteListener { addIt ->
                if (addIt.isSuccessful) {

                    db.collection("Vendors").document(auth.uid!!).collection("LocalClients").whereEqualTo("Number", number).get().addOnCompleteListener {
                        for (i in it.result) {
                            cid = i.id
                        }
                        makeBill()
                    }

                } else {
                    AlertDialog.Builder(context!!).setTitle("Error").setMessage(addIt.exception!!.localizedMessage).show()
                }
            }
        }
        dialogeBuilder.create().show()
    }

    private fun makeBill() {
        val ft = fragmentManager!!.beginTransaction()
        ft.replace(R.id.container, BillFragment(), "BillFragment")
        ft.commit()
    }

    private fun checkNull(view: EditText, string: String): Boolean {
        if (TextUtils.isEmpty(string)) {
            view.error = "Please enter detail"
            return true
        }
        return false
    }

}

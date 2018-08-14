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
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.serviquik.nearby.MainActivity
import com.serviquik.nearby.R
import android.widget.ArrayAdapter


class SignUpDetailsFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var spinner: Spinner
    private var arrayList = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_details, container, false)

        val numberEt = view.findViewById<EditText>(R.id.signUpDetailPhone)
        val addLine1ET = view.findViewById<EditText>(R.id.signUpDetailsAddline1)
        val addLine2ET = view.findViewById<EditText>(R.id.signUpDetailsAddline2)
        spinner = view.findViewById(R.id.signUpDetailSpinner)

        FirebaseFirestore.getInstance().collection("Categories").get().addOnCompleteListener {
            for (document in it.result) {
                arrayList.add(document.id)
            }
            val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, arrayList)
            spinner.adapter = adapter
        }

        view.findViewById<Button>(R.id.loginSignUpBtn).setOnClickListener {
            signUpUser(
                    arguments!!["Email"] as String,
                    arguments!!["Password"] as String,
                    arguments!!["Username"] as String,
                    addLine1ET.text.toString(),
                    addLine2ET.text.toString(),
                    spinner.selectedItem.toString(),
                    numberEt.text.toString()
            )
        }

        return view
    }

    private fun signUpUser(email: String, password: String, userName: String, addLine1: String, addLine2: String, category: String, phoneNumber: String) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val lat = LoginActivity.lat
                val lon = LoginActivity.long
                addDataToDatabase(userName, email, lat, lon, addLine1, addLine2, category, phoneNumber)
            } else {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
            }
        }
    }

    private fun addDataToDatabase(userName: String, email: String, lat: Double?, lon: Double?, addLine1: String, addLine2: String, category: String, phoneNumber: String) {
        val db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any?>()
        data["Address1"] = addLine1
        data["Address2"] = addLine2
        data["Category"] = category
        data["Location"] = GeoPoint(lat!!, lon!!)
        data["Name"] = userName
        data["Number"] = phoneNumber
        data["Email"] = email

        db.collection("Vendors").document().set(data).addOnCompleteListener {
            startActivity(Intent(context!!, MainActivity::class.java))
            activity!!.finish()
        }
    }

}

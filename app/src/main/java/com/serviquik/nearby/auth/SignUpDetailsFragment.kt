package com.serviquik.nearby.auth


import android.location.Address
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
import com.serviquik.nearby.R
import android.widget.ArrayAdapter
import android.location.Geocoder
import android.text.SpannableStringBuilder
import android.text.TextUtils
import java.util.*


class SignUpDetailsFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var spinner: Spinner
    private var arrayList = ArrayList<String>()

    private var address1: String? = null
    private var address2: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_details, container, false)

        val numberEt = view.findViewById<EditText>(R.id.signUpDetailPhone)
        val addLine1ET = view.findViewById<EditText>(R.id.signUpDetailsAddline1)
        val addLine2ET = view.findViewById<EditText>(R.id.signUpDetailsAddline2)
        val titleET = view.findViewById<EditText>(R.id.signUpDetailsTitle)
        spinner = view.findViewById(R.id.signUpDetailSpinner)

        FirebaseFirestore.getInstance().collection("Categories").get().addOnCompleteListener {
            for (document in it.result) {
                arrayList.add(document.id)
            }
            val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, arrayList)
            spinner.adapter = adapter
        }

        getAddressFromLatLon(LoginActivity.lat, LoginActivity.long, addLine1ET, addLine2ET)

        view.findViewById<Button>(R.id.loginSignUpBtn).setOnClickListener {

            val number = numberEt.text.toString()
            val addLine1 = addLine1ET.text.toString()
            val addLine2 = addLine2ET.text.toString()
            val title = titleET.text.toString()

            if (TextUtils.isEmpty(number)) {
                numberEt.error = "Please enter phone number"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(addLine1)) {
                addLine1ET.error = "Please enter address"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(addLine2)) {
                addLine2ET.error = "Please enter address"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(title)) {
                titleET.error = "Please enter title"
                return@setOnClickListener
            }


            signUpUser(arguments!!["Email"] as String, arguments!!["Password"] as String, arguments!!["Username"] as String, addLine1, addLine2, spinner.selectedItem.toString(), number, title)
        }

        return view
    }

    private fun getAddressFromLatLon(lat: Double?, long: Double?, addLine1ET: EditText?, addLine2ET: EditText?) {

        val geocoder = Geocoder(context!!, Locale.getDefault())
        val addresses: List<Address>
        addresses = geocoder.getFromLocation(lat!!, long!!, 1)
        address1 = addresses[0].getAddressLine(0)
        address2 = addresses[0].getAddressLine(1)

        if (address1 != null) {
            addLine1ET!!.text = SpannableStringBuilder(address1)
            if(address2 != null){
                addLine2ET!!.text = SpannableStringBuilder(address2)
            }
        }
    }

    private fun signUpUser(email: String, password: String, userName: String, addLine1: String, addLine2: String, category: String, phoneNumber: String, title: String) {

        val dialog = AlertDialog.Builder(context!!).setTitle("Loading").setMessage("Registering your detail. Thease may take some while").setCancelable(false).show()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val lat = LoginActivity.lat
                val lon = LoginActivity.long
                LoginActivity.addDataToDatabase(userName, email, lat, lon, addLine1, addLine2, category, phoneNumber, context!!, activity!!, title)
            } else {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                dialog.dismiss()
            }
        }
    }


}

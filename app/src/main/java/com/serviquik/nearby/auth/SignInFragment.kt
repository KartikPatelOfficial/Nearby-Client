package com.serviquik.nearby.auth

import android.app.Dialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.serviquik.nearby.R
import java.util.concurrent.TimeUnit
import com.serviquik.nearby.MainActivity
import java.util.*
import kotlin.collections.ArrayList


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SignInFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    private lateinit var okButton: Button
    private lateinit var editText: EditText
    private lateinit var phoneNumber: String

    private var isOTP = false

    var verificationID: String? = null
    var token: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        okButton = view.findViewById(R.id.loginVerifyBtn)
        editText = view.findViewById(R.id.signInPhoneET)

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                signInWithPhoneAuthCredential(p0!!)
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(p0!!.localizedMessage).show()
            }

            override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(p0, p1)
                verificationID = p0
                token = p1
            }

        }

        okButton.setOnClickListener {
            val text = editText.text.toString()

            if (isOTP) {
                phoneNumber = text
                verifyPhoneNumberWithCode(verificationID!!, phoneNumber)
            } else {
                if (text.length != 10) {
                    AlertDialog.Builder(context!!).setTitle("Error").setMessage("Please enter correct mobile number. without any contry code(+91)").show()
                    return@setOnClickListener
                }
                startPhoneNumberVerification("+91$text")
            }
        }

        return view
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid

                        FirebaseFirestore.getInstance().collection("Vendors").document(uid).get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(activity!!, "Welcome ${it.result.getString("Name")}", Toast.LENGTH_LONG).show()
                                startMainActivity()
                            } else {
                                requestToFill()
                            }
                        }

                    } else {
                        AlertDialog.Builder(context!!).setTitle("Error").setMessage(task.exception!!.localizedMessage).show()
                    }
                }
    }

    private fun requestToFill() {

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels

        val dialog = Dialog(context!!)
        dialog.setContentView(R.layout.fragment_sign_up_details)
        dialog.window.setLayout((6 * width) / 7, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog.setTitle("One More Step")

        val nameET = dialog.findViewById<EditText>(R.id.signUpUsername)
        val addLine1ET = dialog.findViewById<EditText>(R.id.signUpAddress)
        val organizationET = dialog.findViewById<EditText>(R.id.signUpOrganization)
        val spinner = dialog.findViewById<Spinner>(R.id.signUpSpinner)

        val arrayList = ArrayList<String>()
        FirebaseFirestore.getInstance().collection("Categories").get().addOnCompleteListener {
            for (document in it.result) {
                arrayList.add(document.id)
            }
        }

        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, arrayList)
        spinner.adapter = adapter

        val address = getAddressFromLatLon(LoginActivity.lat, LoginActivity.long)

        if (address != null) {
            addLine1ET.text = SpannableStringBuilder(address)

        }

        dialog.findViewById<Button>(R.id.loginSignUpBtn).setOnClickListener { _ ->

            val name = nameET.text.toString()
            val addLine = addLine1ET.text.toString()
            val title = organizationET.text.toString()

            if (TextUtils.isEmpty(name)) {
                nameET.error = "Please enter name"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(addLine)) {
                addLine1ET.error = "Please enter address"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(title)) {
                organizationET.error = "Please enter organization name"
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()

            val data = HashMap<String, Any?>()
            data["Address"] = addLine
            data["Category"] = spinner.selectedItem.toString()
            data["Location"] = GeoPoint(LoginActivity.lat!!, LoginActivity.long!!)
            data["Name"] = name
            data["Number"] = phoneNumber
            data["NumberVerifies"] = true
            data["Title"] = title

            db.collection("Vendors").document(FirebaseAuth.getInstance().currentUser!!.uid).set(data).addOnCompleteListener {
                ContextCompat.startActivity(context!!, Intent(context!!, MainActivity::class.java), null)
                activity!!.finish()
            }

        }

        dialog.show()

    }

    private fun getAddressFromLatLon(lat: Double?, long: Double?): String? {
        val geocoder = Geocoder(context!!, Locale.getDefault())
        val addresses: List<Address>
        addresses = geocoder.getFromLocation(lat!!, long!!, 1)
        return addresses[0].getAddressLine(0)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        changeUI()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, activity!!, mCallbacks!!)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun changeUI() {
        okButton.text = getString(R.string.verify)
        editText.text = SpannableStringBuilder("")
        editText.hint = "OTP"
        isOTP = true
    }

    private fun startMainActivity() {
        startActivity(Intent(context!!, MainActivity::class.java))
        activity!!.finish()
    }

}

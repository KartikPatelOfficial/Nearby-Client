package com.serviquik.nearby.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.widget.*
import com.example.easywaylocation.Listener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.EasyWayLocation.LOCATION_SETTING_REQUEST_CODE
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.serviquik.nearby.MainActivity
import com.serviquik.nearby.R


class LoginActivity : AppCompatActivity(), Listener {

    private var easyWayLocation: EasyWayLocation? = null

    private var isFirstTime:Boolean = true

    companion object {
        var lat: Double? = null
        var long: Double? = null

        fun addDataToDatabase(userName: String, email: String, lat: Double?, lon: Double?, addLine1: String, addLine2: String, category: String, phoneNumber: String, context: Context, activity: FragmentActivity, title: String) {
            val db = FirebaseFirestore.getInstance()

            val data = HashMap<String, Any?>()
            data["Address1"] = addLine1
            data["Address2"] = addLine2
            data["Category"] = category
            data["Location"] = GeoPoint(lat!!, lon!!)
            data["Name"] = userName
            data["Number"] = phoneNumber
            data["Email"] = email
            data["Title"] = title

            db.collection("Vendors").document(FirebaseAuth.getInstance().currentUser!!.uid).set(data).addOnCompleteListener {
                startActivity(context, Intent(context, MainActivity::class.java), null)
                activity.finish()

            }
        }
    }

    private val signIn = 69
    private var isLocationPermissionEnabled = false

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadActivity()
    }

    private fun loadActivity() {
        mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            startMainActivity()
        }
        setContentView(R.layout.activity_login)

        val ft = supportFragmentManager!!.beginTransaction()
        if (isFirstTime) {
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            isFirstTime=false
        }
        ft.replace(R.id.LoginFramLayout, SignInFragment(), "RootFragment")
        ft.commit()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<SignInButton>(R.id.loginGoogleSignIn).setOnClickListener {
            signIn()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        } else {
            isLocationPermissionEnabled = true
            easyWayLocation = EasyWayLocation(this)
            easyWayLocation!!.setListener(this)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
                    loadActivity()
                } else {
                    finish()
                }
                return
            }
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, signIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            LOCATION_SETTING_REQUEST_CODE -> easyWayLocation!!.onActivityResult(resultCode)

            signIn -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val arrayList = ArrayList<String>()

        FirebaseFirestore.getInstance().collection("Categories").get().addOnCompleteListener {
            for (document in it.result) {
                arrayList.add(document.id)
            }
        }

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val metrics = resources.displayMetrics
                        val width = metrics.widthPixels

                        val dialog = Dialog(this)
                        dialog.setContentView(R.layout.fragment_sign_up_details)
                        dialog.window.setLayout((6 * width) / 7, LinearLayout.LayoutParams.WRAP_CONTENT)
                        dialog.setTitle("One More Step")

                        val numberEt = dialog.findViewById<EditText>(R.id.signUpDetailPhone)
                        val addLine1ET = dialog.findViewById<EditText>(R.id.signUpDetailsAddline1)
                        val addLine2ET = dialog.findViewById<EditText>(R.id.signUpDetailsAddline2)
                        val titleET = dialog.findViewById<EditText>(R.id.signUpDetailsTitle)
                        val spinner = dialog.findViewById<Spinner>(R.id.signUpDetailSpinner)

                        val adapter = ArrayAdapter<String>(this@LoginActivity, android.R.layout.simple_spinner_dropdown_item, arrayList)
                        spinner.adapter = adapter

                        dialog.findViewById<Button>(R.id.loginSignUpBtn).setOnClickListener {

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

                            addDataToDatabase(mAuth.currentUser!!.displayName!!, mAuth.currentUser!!.email!!, lat, long, addLine1, addLine2, spinner.selectedItem.toString(), number, this, this, title)
                        }

                        dialog.show()

                    } else {
                        AlertDialog.Builder(this).setTitle("Error").setMessage(task.exception!!.localizedMessage).show()
                    }
                }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun locationCancelled() {
        easyWayLocation!!.showAlertDialog("Title", "Message", null)
    }

    override fun locationOn() {
        easyWayLocation!!.beginUpdates()
        lat = easyWayLocation!!.latitude
        long = easyWayLocation!!.longitude
    }

    override fun onPositionChanged() {}

    override fun onPause() {
        super.onPause()
        if (isLocationPermissionEnabled) {
            easyWayLocation!!.endUpdates()
        }
    }
}
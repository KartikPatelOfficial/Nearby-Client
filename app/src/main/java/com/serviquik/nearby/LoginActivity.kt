package com.serviquik.nearby

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
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


class LoginActivity : AppCompatActivity(), Listener {

    var easyWayLocation: EasyWayLocation? = null
    private var lat: Double? = null
    private var long: Double? = null

    private val signIn = 69

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        //Todo Pass user to mainactivity if user already sign in

        setContentView(R.layout.activity_login)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }else{
            easyWayLocation = EasyWayLocation(this)
            easyWayLocation!!.setListener(this)
        }

        val ft = supportFragmentManager!!.beginTransaction()
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
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

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //Todo: Add data to database
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception!!.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
    }

    override fun locationCancelled() {
        easyWayLocation!!.showAlertDialog("Title", "Message", null)
    }

    override fun locationOn() {
        easyWayLocation!!.beginUpdates()
        lat = easyWayLocation!!.latitude
        long = easyWayLocation!!.longitude
        Toast.makeText(this, "Location ON", Toast.LENGTH_SHORT).show()
    }

    override fun onPositionChanged() {
        Toast.makeText(this, "" + easyWayLocation!!.longitude + "," + easyWayLocation!!.latitude, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        easyWayLocation!!.endUpdates()
        super.onPause()
    }
}


package com.serviquik.nearby


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import android.text.InputType
import android.widget.*
import com.serviquik.nearby.auth.LoginActivity


class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()!!

    private lateinit var add1Tv: TextView
    private lateinit var emailTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var titleTV: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val profilePictureTv: ImageView = view.findViewById(R.id.circleImageView)

        db.collection("Vendors").document(auth.currentUser!!.uid).get().addOnCompleteListener { it ->

            val document = it.result
            val profilePicURL = document["ProfilePicture"]

            if (profilePicURL != null) {
                Picasso.get().load(Uri.parse(profilePicURL as String)).into(profilePictureTv)
            } else {
                val authURL: Uri? = auth.currentUser!!.photoUrl
                if (authURL != null) {
                    Picasso.get().load(authURL).into(profilePictureTv)
                }
            }

            addLogoutListner(view)

            view.findViewById<TextView>(R.id.profileNameTV).text = document["Name"] as String
            add1Tv = view.findViewById(R.id.profileAdd1TV)
            add1Tv.text = document["Address1"] as String
            emailTv = view.findViewById(R.id.profileEmailTV)
            emailTv.text = document["Email"] as String
            phoneTv = view.findViewById(R.id.profilePhoneTV)
            phoneTv.text = document["Number"] as String
            titleTV = view.findViewById(R.id.profileTitleTV)
            titleTV.text = document["Title"] as String

            addOnClickListner(view.findViewById(R.id.profileAdd1Edit), "Address1")
            addOnClickListner(view.findViewById(R.id.profilePhoneEdit), "Number")
            addOnClickListner(view.findViewById(R.id.profileTitleEdit), "Title")

        }

        return view
    }

    private fun addLogoutListner(view: View) {
        view.findViewById<Button>(R.id.profileLogoutBtn).setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity!!, LoginActivity::class.java))
            activity!!.finish()
        }
    }

    @SuppressLint("InflateParams")
    private fun addOnClickListner(imageButton: ImageButton, key: String) {

        imageButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(context!!)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.custom_dialog, null)
            dialogBuilder.setView(dialogView)

            val edt = dialogView.findViewById<View>(R.id.edit1) as EditText

            when (key) {
                "Number" -> {
                    edt.inputType = InputType.TYPE_CLASS_NUMBER
                    edt.hint = "Phone Number"
                }

                "Address1" -> edt.hint = "Address Line 1"
                "Address2" -> edt.hint = "Address Line 2"
                "Title" -> edt.hint = "Title"
            }

            dialogBuilder.setTitle("Edit")
            dialogBuilder.setMessage("Enter text below")
            dialogBuilder.setPositiveButton("Done") { _, _ ->
                addDatatoDatabse(edt.text.toString(), key)
            }
            dialogBuilder.setNegativeButton("Cancel") { _, _ ->
                //pass
            }
            val b = dialogBuilder.create()
            b.show()
        }

    }


    private fun addDatatoDatabse(string: String, key: String) {
        val data = HashMap<String, Any>()
        data[key] = string

        db.collection("Vendors").document(auth.currentUser!!.uid).update(data).addOnFailureListener {
            AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.localizedMessage).show()
        }
    }
}



package com.serviquik.nearby


import android.app.ActionBar
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()!!

    private lateinit var add1Tv: TextView
    private lateinit var add2Tv: TextView
    private lateinit var emailTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var titleTV: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val profilePictureTv: ImageView = view.findViewById(R.id.circleImageView)

        db.collection("Vendors").document(auth.currentUser!!.uid).get().addOnCompleteListener {

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

            view.findViewById<TextView>(R.id.profileNameTV).text = document["Name"] as String
            add1Tv = view.findViewById(R.id.profileAdd1TV)
            add1Tv.text = document["Address1"] as String
            add2Tv = view.findViewById(R.id.profileAddline2TV)
            add2Tv.text = document["Address2"] as String
            emailTv = view.findViewById(R.id.profileEmailTV)
            emailTv.text = document["Email"] as String
            phoneTv = view.findViewById(R.id.profilePhoneTV)
            phoneTv.text = document["Number"] as String
            titleTV = view.findViewById(R.id.profileTitleTV)
            titleTV.text = document["Title"] as String



            addEditClickListner(view)

        }

        return view
    }

    private fun addEditClickListner(view: View) {

        val add1Et = view.findViewById<EditText>(R.id.profileAdd1ET)
        val add2Et = view.findViewById<EditText>(R.id.profileAddline2ET)
        val emailEt = view.findViewById<EditText>(R.id.profileEmailET)
        val phoneEt = view.findViewById<EditText>(R.id.profilePhoneET)
        val titleEt = view.findViewById<EditText>(R.id.profileTitleET)

        view.findViewById<ImageButton>(R.id.profileAdd1Edit).setOnClickListener {
            hideView(view.findViewById(R.id.profileAdd1TIL), add1Et)
            addDonePressListner(add1Et, "Address1")
        }
        view.findViewById<ImageButton>(R.id.profileAddline2Edit).setOnClickListener {
            hideView(view.findViewById(R.id.profileAddline2TIL), add2Et)
            addDonePressListner(add2Et, "Address2")
        }
        view.findViewById<ImageButton>(R.id.profileEmailEdit).setOnClickListener {
            hideView(view.findViewById(R.id.profileEmailTIL), emailEt)
            addDonePressListner(emailEt, "Email")
        }
        view.findViewById<ImageButton>(R.id.profilePhoneEdit).setOnClickListener {
            hideView(view.findViewById(R.id.profilePhoneTIL), phoneEt)
            addDonePressListner(phoneEt, "Number")
        }
        view.findViewById<ImageButton>(R.id.profileTitleEdit).setOnClickListener {
            hideView(view.findViewById(R.id.profileTitleTIL), titleEt)
            addDonePressListner(titleEt, "Title")
        }
    }

    private fun addDonePressListner(editText: EditText, key: String) {
        editText.setOnEditorActionListener { p0, p1, p2 ->
            if (p1 == EditorInfo.IME_ACTION_DONE) {
                val data = HashMap<String, Any>()
                data[key] = editText.text.toString()

                db.collection("Vendors").document(auth.currentUser!!.uid).update(data).addOnFailureListener {
                    AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.localizedMessage).show()
                }
            }
            false
        }
    }

    private fun hideView(textView: TextInputLayout, editText: EditText) {
        textView.visibility = View.INVISIBLE
        editText.visibility = View.VISIBLE

        val textViewParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0f)
        val editTextParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, 1f)

        textView.layoutParams = textViewParams
        editText.layoutParams = editTextParams

    }

}

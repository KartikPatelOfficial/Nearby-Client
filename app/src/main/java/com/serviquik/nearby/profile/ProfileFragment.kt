package com.serviquik.nearby.profile


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
import com.serviquik.nearby.R
import com.serviquik.nearby.auth.LoginActivity
import android.widget.Toast
import android.app.Activity.RESULT_OK
import android.graphics.drawable.Drawable
import android.os.Handler
import com.squareup.okhttp.*
import java.io.File
import java.io.IOException
import android.os.Looper
import com.serviquik.nearby.MainActivity
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()!!

    private lateinit var add1Tv: TextView
    private lateinit var emailTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var titleTV: TextView
    private lateinit var profilePicture : ImageView

    private val handler = Handler(Looper.getMainLooper())


    private val galleryRequestCode = 108

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profilePicture = view.findViewById(R.id.circleImageView)

        profilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, galleryRequestCode)
        }

        db.collection("Vendors").document(auth.currentUser!!.uid).get().addOnCompleteListener { it ->

            val document = it.result
            val profilePicURL = document["ProfilePicture"]

            if (profilePicURL != null) {
                Picasso.get().load(Uri.parse(profilePicURL as String)).into(profilePicture)
            } else {
                val authURL: Uri? = auth.currentUser!!.photoUrl
                if (authURL != null) {
                    Picasso.get().load(authURL).into(profilePicture)
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

            addOnClickListener(view.findViewById(R.id.profileAdd1Edit), "Address1")
            addOnClickListener(view.findViewById(R.id.profilePhoneEdit), "Number")
            addOnClickListener(view.findViewById(R.id.profileTitleEdit), "Title")

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
    private fun addOnClickListener(imageButton: ImageButton, key: String) {

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
                addDataToDatabase(edt.text.toString(), key)
            }
            dialogBuilder.setNegativeButton("Cancel") { _, _ ->
                //pass
            }
            val b = dialogBuilder.create()
            b.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == galleryRequestCode && resultCode == RESULT_OK && data != null && data.data != null) {

            val realPath = ImageFilePath.getPath(context, data.data)
            val client = OkHttpClient()
            val file = File(realPath)

            val requestBody = MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("file"), file))
                    .build()

            val request = Request.Builder()
                    .url("https://serviquik.com/persist/")
                    .post(requestBody)
                    .build()

            val call = client.newCall(request)

            call.enqueue(object : Callback {

                override fun onFailure(request: Request?, e: IOException?) {
                    handler.post {
                        AlertDialog.Builder(context!!).setTitle("Error").setMessage(e!!.localizedMessage).show()
                    }
                }

                override fun onResponse(response: Response?) {

                    val r = response!!.body().string()
                    val root = JSONObject(r)
                    val profileURL = root.getString("url")

                    val update = HashMap<String, Any>()
                    update["ProfilePicture"] = profileURL

                    db.collection("Vendors").document(auth.uid!!).update(update).addOnCompleteListener {
                        if (it.isSuccessful) {
                            handler.post {
                                Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                                profilePicture.setImageDrawable(Drawable.createFromPath(realPath))
                                MainActivity.changeNavBarProfilePicture(realPath)
                            }
                        } else {
                            AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                        }

                    }
                }

            })

        } else {
            Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show()
        }

    }

    private fun addDataToDatabase(string: String, key: String) {
        val data = HashMap<String, Any>()
        data[key] = string

        db.collection("Vendors").document(auth.currentUser!!.uid).update(data).addOnFailureListener {
            AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.localizedMessage).show()
        }
    }
}



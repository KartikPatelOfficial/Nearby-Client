@file:Suppress("DEPRECATION")

package com.serviquik.nearby.profile


import android.Manifest
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
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Handler
import com.squareup.okhttp.*
import java.io.File
import java.io.IOException
import android.os.Looper
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.firebase.firestore.GeoPoint
import com.serviquik.nearby.MainActivity
import com.serviquik.nearby.orderList.OrderListFragment
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()!!

    private lateinit var add1Tv: TextView
    private lateinit var emailTv: TextView
    private lateinit var phoneTv: TextView
    private lateinit var titleTV: TextView
    private lateinit var profilePicture: ImageView

    private var easyWayLocation: EasyWayLocation? = null
    var lat: Double? = null
    var long: Double? = null
    var address = ""

    private val handler = Handler(Looper.getMainLooper())


    private val galleryRequestCode = 108

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        } else {
            easyWayLocation = EasyWayLocation(context!!)
            easyWayLocation!!.setListener(object : Listener {
                override fun locationCancelled() {
                    easyWayLocation!!.showAlertDialog("Error", "Please enable location service", null)
                }

                override fun locationOn() {
                    easyWayLocation!!.beginUpdates()
                    lat = easyWayLocation!!.latitude
                    long = easyWayLocation!!.longitude
                    address = getAddressFromLatLon(lat, long)!!
                }

                override fun onPositionChanged() {}

            })
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        MainActivity.changeToolbarTitle("Profile")

        profilePicture = view.findViewById(R.id.circleImageView)
        progressDialog.show()
        profilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, galleryRequestCode)
        }

        var temp = false

        try {
            temp = arguments!!["isNew"] as Boolean
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }

        if (temp) {

            if (temp) {
                val inflater1 = LayoutInflater.from(context!!)
                val dialog = inflater1.inflate(R.layout.alert_profile_required, null)
                val dialogeBuilder = AlertDialog.Builder(context!!)
                dialogeBuilder.setView(dialog)
                dialogeBuilder.setTitle("Hello,")

                val nameEt = dialog.findViewById<EditText>(R.id.profileNameEt)
                val emailEt = dialog.findViewById<EditText>(R.id.profileEmailEt)
                val addressEt = dialog.findViewById<EditText>(R.id.profileAddressEt)
                val titleEt = dialog.findViewById<EditText>(R.id.profileTitleEt)

                val spinner = dialog.findViewById<Spinner>(R.id.profileSpinner)
                val arrayList = ArrayList<String>()
                val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, arrayList)
                spinner.adapter = adapter

                db.collection("Categories").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result) {
                            arrayList.add(doc.id)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                    }
                }

                dialogeBuilder.setCancelable(false)
                dialogeBuilder.setPositiveButton("Ok") { _, _ ->

                    val name = nameEt.text.toString()
                    if (checkNull(name, nameEt, "name")) {
                        return@setPositiveButton
                    }
                    val email = emailEt.text.toString()
                    if (checkNull(email, emailEt, "email")) {
                        return@setPositiveButton
                    }
                    val address = addressEt.text.toString()
                    if (checkNull(address, addressEt, "address")) {
                        return@setPositiveButton
                    }
                    val title = titleEt.text.toString()
                    if (checkNull(title, titleEt, "organization name")) {
                        return@setPositiveButton
                    }
                    val category = spinner.selectedItem.toString()
                    val location = GeoPoint(lat!!, long!!)

                    val data = HashMap<String, Any?>()
                    data["Address1"] = address
                    data["Category"] = category
                    data["Location"] = location
                    data["Name"] = name
                    data["Number"] = arguments!!["phone"]!!.toString()
                    data["Email"] = email
                    data["Title"] = title

                    db.collection("Vendors").document(auth.uid!!).set(data).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val ft = fragmentManager!!.beginTransaction()
                            ft.replace(R.id.container, OrderListFragment())
                            ft.commit()
                        } else {
                            AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                        }
                    }
                }
                dialogeBuilder.show()

            }
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

            view.findViewById<TextView>(R.id.profileNameTV).text = document["Name"] as String?
            add1Tv = view.findViewById(R.id.profileAdd1TV)
            add1Tv.text = document["Address1"] as String?
            emailTv = view.findViewById(R.id.profileEmailTV)
            emailTv.text = document["Email"] as String?
            phoneTv = view.findViewById(R.id.profilePhoneTV)
            phoneTv.text = document["Number"] as String?
            titleTV = view.findViewById(R.id.profileTitleTV)
            titleTV.text = document["Title"] as String?

            addOnClickListener(view.findViewById(R.id.profileAdd1Edit), "Address1")
            addOnClickListener(view.findViewById(R.id.profilePhoneEdit), "Number")
            addOnClickListener(view.findViewById(R.id.profileTitleEdit), "Title")
            addOnClickListener(view.findViewById(R.id.profileEmailEdit), "Email")

            progressDialog.dismiss()
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
            val til = dialogView.findViewById<TextInputLayout>(R.id.editTIL)

            when (key) {
                "Number" -> {
                    edt.inputType = InputType.TYPE_CLASS_NUMBER
                    edt.hint = "Phone Number"
                    til.hint = "Phone Number"
                }

                "Address1" -> {
                    edt.hint = "Address Line"
                    til.hint = "Address Line"
                }
                "Title" -> {
                    edt.hint = "Title"
                    til.hint = "Title"

                }
                "Email" -> {
                    edt.hint = "Email"
                    til.hint = "Email"
                }
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

            progressDialog.show()

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
                        progressDialog.dismiss()

                    }
                }

            })

        } else {
            Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkNull(text: String, editText: EditText, title: String): Boolean {
        if (TextUtils.isEmpty(text)) {
            editText.error = "Please enter our $title"
            return true
        }
        return false
    }

    private fun getAddressFromLatLon(lat: Double?, long: Double?): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>
        addresses = geocoder.getFromLocation(lat!!, long!!, 1)
        return addresses[0].getAddressLine(0)
    }

    private fun addDataToDatabase(string: String, key: String) {
        val data = HashMap<String, Any>()
        data[key] = string
        progressDialog.show()
        db.collection("Vendors").document(auth.currentUser!!.uid).update(data).addOnCompleteListener {
            progressDialog.dismiss()
            if (!it.isSuccessful) {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
            }
        }
    }
}



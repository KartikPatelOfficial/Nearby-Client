package com.serviquik.nearby.offer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R
import com.serviquik.nearby.manageProduct.Product
import com.serviquik.nearby.profile.ImageFilePath
import com.squareup.okhttp.*
import org.json.JSONObject
import java.io.File
import java.io.IOException

class OfferFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val products = ArrayList<Product>()
    private val productsMap = HashMap<String, Product>()
    private val productsNames = ArrayList<String>()
    private val requestGalleryCode = 69
    private lateinit var file: File

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private val offers = ArrayList<Offer>()
        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: OfferAdapter
    }

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_offer, container, false)

        adapter = OfferAdapter(offers, context!!)

        val recyclerView = view.findViewById<RecyclerView>(R.id.offerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        progressDialog.show()

        db.collection("Products").whereEqualTo("VendorID", auth.uid).get().addOnCompleteListener { productIt ->
            if (productIt.isSuccessful) {
                for (doc in productIt.result) {

                    var rating: String? = null
                    try {
                        rating = doc.getString("Rating")
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                    }

                    val product = Product(
                            doc.getString("Description")!!,
                            doc.getString("Name")!!,
                            doc.getLong("Price")!!,
                            null,
                            doc.id,
                            rating,
                            doc.getString("ParentCategory")!!,
                            doc.getTimestamp("Time"))
                    products.add(product)
                    productsMap[product.parentID] = product
                }
                for (product in products) {
                    productsNames.add(product.title)
                }

                db.collection("Vendors").document(auth.uid!!).collection("Offers").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result) {
                            val id = doc.getString("ProductID")
                            if (offers.size < it.result.size()) {
                                offers.add(Offer(doc.getString("Offer"), doc.getString("ImageURL"), productsMap[id]!!, doc.id))
                            }
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                    }
                }

            } else {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(productIt.exception!!.localizedMessage).show()
                return@addOnCompleteListener
            }

            progressDialog.dismiss()
        }

        view.findViewById<FloatingActionButton>(R.id.offerFab).setOnClickListener { _ ->

            val inflater1 = LayoutInflater.from(context!!)
            val dialog = inflater1.inflate(R.layout.alert_offer_add, null)
            val dialogeBuilder = AlertDialog.Builder(context!!)
            dialogeBuilder.setView(dialog)
            dialogeBuilder.setTitle("Add Offer")

            val spinner = dialog.findViewById<Spinner>(R.id.alertOfferSpinner)
            val offerEt = dialog.findViewById<EditText>(R.id.alertOfferOffer)

            val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, productsNames)
            spinner.adapter = adapter

            dialog.findViewById<Button>(R.id.alertOfferPictureBtn).setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, requestGalleryCode)
            }

            dialogeBuilder.setPositiveButton("Add") { _, _ ->
                progressDialog.show()
                val client = OkHttpClient()
                val offer = offerEt.text.toString()
                val product = products[spinner.selectedItemPosition]

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
                        addToDatabase(Offer(offer, profileURL, product, null))
                    }

                })


            }

            dialogeBuilder.show()
        }

        return view
    }

    private fun addToDatabase(offer: Offer) {
        val data = HashMap<String, Any?>()
        data["Offer"] = offer.offer
        data["ImageURL"] = offer.picture
        data["ProductID"] = offer.product.parentID

        db.collection("Vendors").document(auth.uid!!).collection("Offers").document().set(data).addOnCompleteListener {
            if (it.isSuccessful) {
                offers.add(offer)
                adapter.notifyItemChanged(offers.size)
                db.collection("Products").document(offer.product.parentID).collection("offers").document().set(data)
            } else {
                handler.post {
                    AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                }
            }
            progressDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestGalleryCode && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val realPath = ImageFilePath.getPath(context, data.data)
            file = File(realPath)
        }
    }

}

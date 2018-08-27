@file:Suppress("UNUSED_VARIABLE")

package com.serviquik.nearby.manageProduct

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R
import android.widget.AdapterView
import com.google.firebase.Timestamp
import com.serviquik.nearby.profile.ImageFilePath
import com.squareup.okhttp.*
import org.json.JSONObject
import java.io.File
import java.io.IOException


class ManageProductsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()!!
    private val products = ArrayList<Product>()

    private lateinit var bottomAdapter: ProductBottomAdapter
    private val imagePaths = ArrayList<String>()
    private val files = ArrayList<File>()
    private var currentUpload = 0

    private val galleryRequestCode = 69

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_products, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.productsRecyclerView)
        val adapter = ProductsAdapter(products, fragmentManager!!)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val arrayList = ArrayList<String>()

        db.collection("Vendors").document(auth.uid!!).get().addOnCompleteListener { rootIt ->
            if (rootIt.isSuccessful) {
                val category = rootIt.result.getString("Category")
                db.collection("Categories").document(category!!).collection("Subcategories").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result) {
                            arrayList.add(doc.id)
                        }
                    }
                }
            }
        }

        view.findViewById<View>(R.id.manageProductFAB).setOnClickListener { _ ->

            val inflater1 = LayoutInflater.from(context!!)
            val dialog = inflater1.inflate(R.layout.product_add, null)
            val dialogeBuilder = AlertDialog.Builder(context!!)
            dialogeBuilder.setView(dialog)
            dialogeBuilder.setTitle("Fill data")

            val nameEt = dialog.findViewById<EditText>(R.id.productAddName)
            val descriptionEt = dialog.findViewById<EditText>(R.id.productAddDescription)
            val priceEt = dialog.findViewById<EditText>(R.id.productAddPrice)
            val spinner: Spinner = dialog.findViewById(R.id.productAddSpinner)
            val imageRecyclerView: RecyclerView = dialog.findViewById(R.id.productAddRVImage)

            dialog.findViewById<Button>(R.id.productAddSelectImage).setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, galleryRequestCode)
            }

            imageRecyclerView.layoutManager = GridLayoutManager(context, 4)
            bottomAdapter = ProductBottomAdapter(imagePaths)
            imageRecyclerView.adapter = bottomAdapter

            val arrayAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, arrayList)
            spinner.adapter = arrayAdapter

            var category: String? = null

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    category = arrayList[p2]
                }

            }
            dialogeBuilder.setPositiveButton("OK") { _, _ ->

                val client = OkHttpClient()
                val requests = ArrayList<Request>()

                val description = descriptionEt.text.toString()
                val name = nameEt.text.toString()
                val price = Integer.parseInt(priceEt.text.toString())
                val time = Timestamp.now()

                for ((i, file) in files.withIndex()) {

                    val requestBody = MultipartBuilder()
                            .type(MultipartBuilder.FORM)
                            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("file"), file))
                            .build()

                    val request = com.squareup.okhttp.Request.Builder()
                            .url("https://serviquik.com/persist/")
                            .post(requestBody)
                            .build()

                    val call = client.newCall(request)

                    call.enqueue(object : Callback {
                        override fun onFailure(request: Request?, e: IOException?) {
                            AlertDialog.Builder(context!!).setTitle("Error").setMessage(e!!.localizedMessage).show()
                        }

                        override fun onResponse(response: Response?) {

                            val r = response!!.body().string()
                            val root = JSONObject(r)
                            val profileURL = root.getString("url")
                            imagePaths.add(profileURL)

                            if (currentUpload == i) {
                                val data = HashMap<String, Any?>()
                                data["Name"] = name
                                data["Description"] = description
                                data["Price"] = price
                                data["ParentCategory"] = category
                                data["Time"] = time
                                data["Image"] = imagePaths
                                data["VendorID"] = auth.uid

                                db.collection("Products").document().set(data).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        products.add(Product(description, name, price.toLong(), null, auth.uid!!, null, category!!, time))
                                        arrayAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    })
                }

            }

            dialogeBuilder.show()
        }

        db.collection("Products").whereEqualTo("VendorID", auth.currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result) {
                    @Suppress("UNCHECKED_CAST")

                    var imageList = ArrayList<String>()

                    try {
                        @Suppress("UNCHECKED_CAST")
                        imageList = document["Image"] as ArrayList<String>
                    } catch (e: TypeCastException) {

                    }

                    val product = Product(
                            document.getString("Description")!!,
                            document.getString("Name")!!,
                            document.getLong("Price")!!,
                            imageList,
                            document.id,
                            document.getString("rating"),
                            document.getString("ParentCategory")!!,
                            document.getTimestamp("Time")
                    )
                    if (products.size < it.result.size()) {
                        products.add(product)
                        adapter.notifyDataSetChanged()
                    }
                }
            } else {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
            }
        }

        return view

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == galleryRequestCode && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val realPath = ImageFilePath.getPath(context, data.data)
            files.add(File(realPath))
            bottomAdapter.notifyItemChanged(files.size)
        }

    }

}

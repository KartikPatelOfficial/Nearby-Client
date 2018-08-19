package com.serviquik.nearby.manageProduct

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
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


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ManageProductsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()!!
    private val products = ArrayList<Product>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_products, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.productsRecyclerView)
        val adapter = ProductsAdapter(products,fragmentManager!!)
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

            val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, arrayList)
            spinner.adapter = adapter

            var category: String? = null

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    category = arrayList[p2]
                }

            }
            dialogeBuilder.setPositiveButton("OK") { _, _ ->

                val description = descriptionEt.text.toString()
                val name = nameEt.text.toString()
                val price = Integer.parseInt(priceEt.text.toString())
                val time = Timestamp.now()

                val data = HashMap<String, Any?>()
                data["Name"] = name
                data["Description"] = description
                data["Price"] = price
                data["ParentCategory"] = category
                data["Time"] = time
                data["VendorID"] = auth.uid

                db.collection("Products").document().set(data).addOnCompleteListener {
                    if (it.isSuccessful) {
                        products.add(Product(description, name, price.toLong(), null, auth.uid!!, null, category!!, time))
                        adapter.notifyDataSetChanged()
                    }
                }

            }

            dialogeBuilder.show()
        }

        db.collection("Products").whereEqualTo("VendorID", auth.currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result) {
                    @Suppress("UNCHECKED_CAST")

                    val imageList = ArrayList<String>()

                    try {
                        val imageList = document["Image"] as ArrayList<String>
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
                    products.add(product)
                    adapter.notifyDataSetChanged()
                }
            } else {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
            }
        }

        return view

    }
}

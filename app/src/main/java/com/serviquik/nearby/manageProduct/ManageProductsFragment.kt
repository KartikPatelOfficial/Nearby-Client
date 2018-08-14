package com.serviquik.nearby.manageProduct

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R


class ManageProductsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()!!
    private val products = ArrayList<Product>()
    private val adapter = ProductsAdapter(products)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_products, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.productsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        db.collection("Products").whereEqualTo("VendorID", auth.currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result) {
                    Log.d("----->", document["VendorID"].toString())
                    @Suppress("UNCHECKED_CAST")
                    val product = Product(
                            document.getString("Description")!!,
                            document.getString("Name")!!,
                            document.getLong("Price")!!,
                            document.getLong("DiscountPrice")!!,
                            document["Image"] as ArrayList<String>,
                            document.id,
                            document.getString("rating")!!
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

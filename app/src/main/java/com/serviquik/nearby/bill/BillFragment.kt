package com.serviquik.nearby.bill

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.serviquik.nearby.R
import com.serviquik.nearby.customer.ManageCustomerFragment
import com.serviquik.nearby.manageProduct.Product

@SuppressLint("StaticFieldLeak")
class BillFragment : Fragment() {

    val name = ManageCustomerFragment.name
    val number = ManageCustomerFragment.number
    val email = ManageCustomerFragment.email


    companion object {
        var total:Long = 0
        var count = 0
        val currentProducts = ArrayList<Bill>()
        var adapter: BillAdapter? = null

        lateinit var totalText: TextView

        fun makeTotal(price: Long, quantity: Int): Long {
            return total + (price * quantity)
        }
    }

    private val products = ArrayList<Product>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bill, container, false)
        adapter = BillAdapter(products, context!!)
        val recyclerView: RecyclerView = view.findViewById(R.id.billRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.adapter = adapter

        val text = "₹" + makeTotal(0, 0)
        totalText = view.findViewById(R.id.billTotalTV)
        totalText.text = text

        db.collection("Products").whereEqualTo("VendorID", auth.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (document in it.result) {
                    products.add(
                            Product(document["Description"] as String, document["Name"] as String, document.getLong("Price")!!, null, document.id, null, document.getString("ParentCategory")!!, document.getTimestamp("Time"))
                    )
                }
                adapter!!.notifyDataSetChanged()
            }
        }

        return view
    }



}

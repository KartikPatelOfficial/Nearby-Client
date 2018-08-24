package com.serviquik.nearby.orderList

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.serviquik.nearby.R
import com.serviquik.nearby.bill.Bill

class OrderListFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val orders = ArrayList<Order>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_order_list, container, false)

        val adapter = OrderAdapter(orders,context!!)

        val recyclerView: RecyclerView = view.findViewById(R.id.orderRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context!!)

        val orderRef = db.collection("Vendors").document(auth.uid!!).collection("OrderList")

        orderRef.get().addOnCompleteListener { orderIt ->
            if (orderIt.isSuccessful) {
                for ((i, doc) in orderIt.result.withIndex()) {
                    val order = Order()
                    order.name = doc.getString("Name")!!
                    order.id = doc.getString("CID")!!
                    order.date = doc.getTimestamp("Time")!!
                    order.amount = doc.getLong("Price")!!
                    order.status = doc.getLong("Status")!!
                    orders.add(order)
                    adapter.notifyDataSetChanged()
                    orderRef.document(doc.id).collection("Items").get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            val items = ArrayList<Bill>()
                            for (item in it.result) {
                                items.add(Bill(item.getString("Name"), item.getLong("Price"), item.getLong("Quantity")!!.toInt()))
                            }
                            orders[i].items = items
                            adapter.notifyDataSetChanged()
                            try {
                                recyclerView.adapter = OrderAdapter(orders,activity!!)
                            }catch (e:KotlinNullPointerException){
                                e.printStackTrace()
                            }
                        } else {
                            AlertDialog.Builder(context!!).setTitle("Error").setMessage(it.exception!!.localizedMessage).show()
                        }
                    }
                }
            } else {
                AlertDialog.Builder(context!!).setTitle("Error").setMessage(orderIt.exception!!.localizedMessage).show()
            }
        }

        return view
    }


}

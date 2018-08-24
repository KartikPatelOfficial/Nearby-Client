package com.serviquik.nearby.orderList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.serviquik.nearby.R
import com.serviquik.nearby.bill.Bill

class BottomOrderAdapter(private val bills: ArrayList<Bill>) : RecyclerView.Adapter<OrderViewholder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): OrderViewholder {
        return OrderViewholder(LayoutInflater.from(p0.context).inflate(R.layout.card_bottom_order, p0, false))
    }

    override fun getItemCount(): Int {
        return bills.size
    }

    override fun onBindViewHolder(p0: OrderViewholder, p1: Int) {
        val bill = bills[p1]
        val price = "₹${bill.productPrice}"

        p0.nameTv.text = bill.productName
        p0.priceTv.text = price
        p0.quantityTv.text = bill.quantity.toString()
    }

}

class OrderViewholder(view: View) : RecyclerView.ViewHolder(view) {
    val nameTv = view.findViewById<TextView>(R.id.cardBottomOrderName)!!
    val quantityTv = view.findViewById<TextView>(R.id.cardBottomOrderQuantity)!!
    val priceTv = view.findViewById<TextView>(R.id.cardBottomOrderPrice)!!
}
package com.serviquik.nearby.orderList

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.design.card.MaterialCardView
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.serviquik.nearby.R
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val orders: ArrayList<Order>, private val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.card_order_list, p0, false))
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val order = orders[p1]

        val date = SimpleDateFormat("dd/MM/yy hh:mm aa", Locale.ENGLISH).format(order.date.toDate())
        var items = ""
        val amount = "₹${order.amount}"
        for ((i, item) in order.items.withIndex()) {
            items += item.productName + "" + item.quantity
            if (i != order.items.size) {
                items += " · "
            }
        }

        p0.orderCard.setBackgroundColor(getColor(order.status.toInt()))
        p0.nameTV.text = order.name
        p0.dateTV.text = date
        p0.itemTV.text = items
        p0.amountTV.text = amount
        p0.statusTV.text = getStatus(order.status.toInt())

        p0.orderCard.setOnClickListener {
            val dialog = BottomSheetDialog(context)
            dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.bottom_order_card, null, false))

            dialog.findViewById<TextView>(R.id.bottomOrderName)!!.text = order.name
            dialog.findViewById<TextView>(R.id.bottomOrderPrice)!!.text = order.amount.toString()
            dialog.findViewById<TextView>(R.id.bottomOrderStatus)!!.text = getStatus(order.status.toInt())

            val recyclerView = dialog.findViewById<RecyclerView>(R.id.bottomOrderRecyclerView)
            recyclerView!!.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = BottomOrderAdapter(order.items)

            dialog.show()
        }
    }

    private fun getColor(id: Int): Int {

        when (id) {
            Order.APPROVED -> return Color.parseColor(Order.APPROVEDCOLOR)
            Order.PROGRESSED -> return Color.parseColor(Order.PROGRESSEDCOLOR)
            Order.PENDING -> return Color.parseColor(Order.PENDINGCOLOR)
            Order.CANCELLED -> return Color.parseColor(Order.CANCELLEDCOLOR)
            Order.REJECTED -> return Color.parseColor(Order.REJECTEDCOLOR)
            Order.COMPLETED -> return Color.parseColor(Order.COMPLETEDCOLOR)
        }
        return Color.parseColor(Order.REJECTEDCOLOR)
    }

    private fun getStatus(id: Int): String {

        when (id) {
            Order.APPROVED -> return "Approved"
            Order.PROGRESSED -> return "Progressed"
            Order.PENDING -> return "Pending"
            Order.CANCELLED -> return "Cancelled"
            Order.REJECTED -> return "Rejected"
            Order.COMPLETED -> return "Completed"
        }
        return "Unknown"
    }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val nameTV: TextView = view.findViewById(R.id.orderCardName)
    val dateTV: TextView = view.findViewById(R.id.orderCardDate)
    val itemTV: TextView = view.findViewById(R.id.orderCardItems)
    val amountTV: TextView = view.findViewById(R.id.orderCardAmount)
    val statusTV: TextView = view.findViewById(R.id.orderCardStatus)
    val orderCard: MaterialCardView = view.findViewById(R.id.orderCard)
}
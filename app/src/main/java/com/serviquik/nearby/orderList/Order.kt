package com.serviquik.nearby.orderList

import com.google.firebase.Timestamp
import com.serviquik.nearby.bill.Bill


data class Order(var name: String = "Name",
                 var id: String = "3i1r69tq",
                 var date: Timestamp = Timestamp.now(),
                 var amount: Long = 0,
                 var items: ArrayList<Bill> = ArrayList(),
                 var status: Long = Order.REJECTED.toLong()
) {

    companion object {
        val PROGRESSED = 0
        val APPROVED = 1
        val PENDING = 2
        val CANCELLED = 3
        val REJECTED = 4
        val COMPLETED = 5

        val PROGRESSEDCOLOR = "#FBC02D"
        val APPROVEDCOLOR = "#03A9F4"
        val PENDINGCOLOR = "#E91E63"
        val CANCELLEDCOLOR = "#FF5722"
        val REJECTEDCOLOR = "#F44336"
        val COMPLETEDCOLOR = "#00C853"

    }
}
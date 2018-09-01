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
        const val PROGRESSED = 0
        const val APPROVED = 1
        const val PENDING = 2
        const val CANCELLED = 3
        const val REJECTED = 4
        const val COMPLETED = 5

        const val PROGRESSEDCOLOR = "#FBC02D"
        const val APPROVEDCOLOR = "#03A9F4"
        const val PENDINGCOLOR = "#E91E63"
        const val CANCELLEDCOLOR = "#FF5722"
        const val REJECTEDCOLOR = "#F44336"
        const val COMPLETEDCOLOR = "#00C853"

    }
}
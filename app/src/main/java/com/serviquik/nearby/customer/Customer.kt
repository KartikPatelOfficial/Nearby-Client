package com.serviquik.nearby.customer

data class Customer(
        val name: String,
        val email: String,
        val phoneNumber: String,
        val cid:String,
        val isLocal:Boolean,
        val profileURL:String?
)
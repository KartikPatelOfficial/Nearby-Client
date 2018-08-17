package com.serviquik.nearby.manageProduct

import com.google.firebase.Timestamp

class Product(
        val description: String,
        val title: String,
        val price: Long,
        val images: ArrayList<String>?,
        val parentID: String,
        val rating: String?,
        val parantCategory:String,
        val time: Timestamp?
)
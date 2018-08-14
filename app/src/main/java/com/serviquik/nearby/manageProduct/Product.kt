package com.serviquik.nearby.manageProduct

class Product(
        val description: String,
        val title: String,
        val price: Long,
        val disPrice: Long?,
        val images: ArrayList<String>,
        val parentID: String,
        val rating: String
)
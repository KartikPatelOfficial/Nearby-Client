package com.serviquik.nearby.offer

import com.serviquik.nearby.manageProduct.Product

data class Offer(
        val offer: String?,
        val picture: String?,
        val product: Product,
        val id: String?
)
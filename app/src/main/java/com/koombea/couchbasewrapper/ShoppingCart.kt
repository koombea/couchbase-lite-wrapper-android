package com.koombea.couchbasewrapper

import com.koombea.couchbasewrapper.Product

data class ShoppingCart(
    val id: String,
    val date: String,
    val product: Product
)

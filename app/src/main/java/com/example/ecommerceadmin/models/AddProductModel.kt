package com.example.ecommerceadmin.models

data class AddProductModel(
    val productName:String? = "",
    val productDescription:String? = "",
    val productCoverImg:String? = "",
    val productCategory:String? = "",
    val productId:String? = "",
    val productMrp:String? = "",
    val productSp:String? = "",
    val productImage:ArrayList<String>? = null
)

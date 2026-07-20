package com.farm.layermanager.domain.model

import java.time.LocalDate

data class Customer(
    val customerId: Long = 0,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null
)

data class Sale(
    val saleId: Long = 0,
    val sDate: LocalDate,
    val customerId: Long,
    val whiteTrays: Double = 0.0,
    val redTrays: Double = 0.0,
    val crackedTrays: Double = 0.0,
    val whitePrice: Double = 0.0,
    val redPrice: Double = 0.0,
    val crackedPrice: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double,
    val paymentMethod: String? = null,
    val notes: String? = null
) {
    val totalTrays: Double get() = whiteTrays + redTrays + crackedTrays
}

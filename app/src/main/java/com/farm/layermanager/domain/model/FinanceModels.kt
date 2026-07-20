package com.farm.layermanager.domain.model

import java.time.LocalDate

data class ExpenseCategory(
    val categoryId: Long = 0,
    val categoryName: String
)

/** houseId = null يعني مصروف عام على المزرعة (يُوزَّع لاحقاً حسب نسبة الطيور — قسم 5.9). */
data class Expense(
    val expenseId: Long = 0,
    val eDate: LocalDate,
    val categoryId: Long,
    val houseId: Long? = null,
    val amount: Double,
    val description: String? = null
)

data class RevenueType(
    val revenueTypeId: Long = 0,
    val typeName: String
)

data class Revenue(
    val revenueId: Long = 0,
    val rDate: LocalDate,
    val revenueTypeId: Long,
    val amount: Double,
    val description: String? = null
)

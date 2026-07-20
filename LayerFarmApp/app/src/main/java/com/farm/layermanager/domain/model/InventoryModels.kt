package com.farm.layermanager.domain.model

import java.time.LocalDate

enum class InventoryCategory { MEDICINE, VACCINE, DISINFECTANT, CONSUMABLE, OTHER }
enum class InventoryTransactionType { IN, OUT }

data class InventoryItem(
    val itemId: Long = 0,
    val category: InventoryCategory,
    val itemName: String,
    val unit: String,
    val currentStock: Double = 0.0,
    val minThreshold: Double = 0.0
) {
    val isBelowThreshold: Boolean get() = currentStock <= minThreshold
}

data class InventoryTransaction(
    val transactionId: Long = 0,
    val itemId: Long,
    val tDate: LocalDate,
    val type: InventoryTransactionType,
    val quantity: Double,
    val referenceType: String? = null,
    val referenceId: Long? = null,
    val notes: String? = null
)

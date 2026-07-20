package com.farm.layermanager.data.mapper

import com.farm.layermanager.data.local.entity.InventoryItemEntity
import com.farm.layermanager.data.local.entity.InventoryTransactionEntity
import com.farm.layermanager.domain.model.InventoryCategory
import com.farm.layermanager.domain.model.InventoryItem
import com.farm.layermanager.domain.model.InventoryTransaction
import com.farm.layermanager.domain.model.InventoryTransactionType

fun InventoryItemEntity.toDomain(): InventoryItem = InventoryItem(
    itemId = itemId,
    category = runCatching { InventoryCategory.valueOf(category) }.getOrDefault(InventoryCategory.OTHER),
    itemName = itemName,
    unit = unit,
    currentStock = currentStock,
    minThreshold = minThreshold
)

fun InventoryItem.toEntity(): InventoryItemEntity = InventoryItemEntity(
    itemId = itemId,
    category = category.name,
    itemName = itemName,
    unit = unit,
    currentStock = currentStock,
    minThreshold = minThreshold
)

fun InventoryTransactionEntity.toDomain(): InventoryTransaction = InventoryTransaction(
    transactionId = transactionId,
    itemId = itemId,
    tDate = tDate,
    type = runCatching { InventoryTransactionType.valueOf(type) }.getOrDefault(InventoryTransactionType.IN),
    quantity = quantity,
    referenceType = referenceType,
    referenceId = referenceId,
    notes = notes
)

fun InventoryTransaction.toEntity(): InventoryTransactionEntity = InventoryTransactionEntity(
    transactionId = transactionId,
    itemId = itemId,
    tDate = tDate,
    type = type.name,
    quantity = quantity,
    referenceType = referenceType,
    referenceId = referenceId,
    notes = notes
)

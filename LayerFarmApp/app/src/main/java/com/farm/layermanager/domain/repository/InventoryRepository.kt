package com.farm.layermanager.domain.repository

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.model.InventoryItem
import com.farm.layermanager.domain.model.InventoryTransaction
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun insertItem(item: InventoryItem): Long
    suspend fun updateItem(item: InventoryItem)
    fun getAllItems(): Flow<List<InventoryItem>>
    fun getItemsByCategory(category: String): Flow<List<InventoryItem>>
    fun getItemsBelowThreshold(): Flow<List<InventoryItem>>

    /** يحدّث الرصيد ويسجّل الحركة ضمن Transaction واحدة، ويرفض أي صادر يجعل الرصيد سالباً (قسم 5.7). */
    suspend fun recordTransaction(transaction: InventoryTransaction): DomainResult<Long>

    fun getTransactionsByItem(itemId: Long): Flow<List<InventoryTransaction>>
    fun getAllTransactions(): Flow<List<InventoryTransaction>>
}

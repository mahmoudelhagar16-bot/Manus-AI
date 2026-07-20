package com.farm.layermanager.domain.usecase.inventory

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.common.Validator
import com.farm.layermanager.domain.model.InventoryItem
import com.farm.layermanager.domain.model.InventoryTransaction
import com.farm.layermanager.domain.model.InventoryTransactionType
import com.farm.layermanager.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow

class AddInventoryItemUseCase(private val repository: InventoryRepository) {
    suspend operator fun invoke(item: InventoryItem): DomainResult<Long> {
        Validator.requireNotBlank(item.itemName, "اسم الصنف")?.let { return DomainResult.Error(it) }
        Validator.requireNotBlank(item.unit, "الوحدة")?.let { return DomainResult.Error(it) }
        Validator.requireNonNegative(item.minThreshold, "الحد الأدنى")?.let { return DomainResult.Error(it) }

        val id = repository.insertItem(item)
        return DomainResult.Success(id)
    }
}

/** UC-11: حركة وارد/صادر — يرفض أي صادر يجعل الرصيد سالباً (قسم 5.7). */
class RecordInventoryTransactionUseCase(private val repository: InventoryRepository) {
    suspend operator fun invoke(transaction: InventoryTransaction): DomainResult<Long> {
        Validator.requirePositive(transaction.quantity, "الكمية")?.let { return DomainResult.Error(it) }

        return repository.recordTransaction(transaction)
    }
}

class GetInventoryUseCase(private val repository: InventoryRepository) {
    fun getAllItems(): Flow<List<InventoryItem>> = repository.getAllItems()
    fun getItemsByCategory(category: String): Flow<List<InventoryItem>> = repository.getItemsByCategory(category)
    fun getItemsBelowThreshold(): Flow<List<InventoryItem>> = repository.getItemsBelowThreshold()
    fun getTransactionsByItem(itemId: Long): Flow<List<InventoryTransaction>> = repository.getTransactionsByItem(itemId)
}

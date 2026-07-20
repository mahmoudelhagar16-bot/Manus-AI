package com.farm.layermanager.data.repository

import com.farm.layermanager.data.local.dao.InventoryItemDao
import com.farm.layermanager.data.local.dao.InventoryTransactionCompositeDao
import com.farm.layermanager.data.local.dao.InventoryTransactionDao
import com.farm.layermanager.data.mapper.toDomain
import com.farm.layermanager.data.mapper.toEntity
import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.model.InventoryItem
import com.farm.layermanager.domain.model.InventoryTransaction
import com.farm.layermanager.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val itemDao: InventoryItemDao,
    private val transactionDao: InventoryTransactionDao,
    private val compositeDao: InventoryTransactionCompositeDao
) : InventoryRepository {

    override suspend fun insertItem(item: InventoryItem): Long = itemDao.insert(item.toEntity())

    override suspend fun updateItem(item: InventoryItem) = itemDao.update(item.toEntity())

    override fun getAllItems(): Flow<List<InventoryItem>> = itemDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getItemsByCategory(category: String): Flow<List<InventoryItem>> =
        itemDao.getByCategory(category).map { list -> list.map { it.toDomain() } }

    override fun getItemsBelowThreshold(): Flow<List<InventoryItem>> =
        itemDao.getBelowThreshold().map { list -> list.map { it.toDomain() } }

    override suspend fun recordTransaction(transaction: InventoryTransaction): DomainResult<Long> {
        val result = compositeDao.recordTransaction(transaction.toEntity())
        return result.fold(
            onSuccess = { id -> DomainResult.Success(id) },
            onFailure = { DomainResult.Error(it.message ?: "فشل تسجيل حركة المخزون") }
        )
    }

    override fun getTransactionsByItem(itemId: Long): Flow<List<InventoryTransaction>> =
        transactionDao.getByItem(itemId).map { list -> list.map { it.toDomain() } }

    override fun getAllTransactions(): Flow<List<InventoryTransaction>> =
        transactionDao.getAll().map { list -> list.map { it.toDomain() } }
}

package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.farm.layermanager.data.local.entity.InventoryItemEntity
import com.farm.layermanager.data.local.entity.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {
    @Insert
    suspend fun insert(item: InventoryItemEntity): Long

    @Update
    suspend fun update(item: InventoryItemEntity)

    @Query("SELECT * FROM inventory_items ORDER BY category, itemName")
    fun getAll(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE category = :category ORDER BY itemName")
    fun getByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE itemId = :itemId")
    suspend fun getByIdOnce(itemId: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE currentStock <= minThreshold")
    fun getBelowThreshold(): Flow<List<InventoryItemEntity>>

    @Query("UPDATE inventory_items SET currentStock = :newStock WHERE itemId = :itemId")
    suspend fun updateStock(itemId: Long, newStock: Double)
}

@Dao
interface InventoryTransactionDao {
    @Insert
    suspend fun insertRaw(transaction: InventoryTransactionEntity): Long

    @Query("SELECT * FROM inventory_transactions WHERE itemId = :itemId ORDER BY tDate DESC")
    fun getByItem(itemId: Long): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions ORDER BY tDate DESC")
    fun getAll(): Flow<List<InventoryTransactionEntity>>
}

/**
 * DAO مركّب: كل حركة (وارد/صادر) تُحدِّث currentStock في نفس الـ Transaction،
 * ويُمنع أي صادر يجعل الرصيد سالباً (قسم 5.7).
 */
@Dao
abstract class InventoryTransactionCompositeDao {

    protected abstract fun itemDao(): InventoryItemDao
    protected abstract fun transactionDao(): InventoryTransactionDao

    @Transaction
    open suspend fun recordTransaction(transaction: InventoryTransactionEntity): Result<Long> {
        val item = itemDao().getByIdOnce(transaction.itemId)
            ?: return Result.failure(IllegalStateException("الصنف غير موجود في المخزون"))

        val newStock = when (transaction.type) {
            "IN" -> item.currentStock + transaction.quantity
            "OUT" -> item.currentStock - transaction.quantity
            else -> return Result.failure(IllegalArgumentException("نوع حركة غير معروف"))
        }

        if (newStock < 0) {
            return Result.failure(IllegalStateException("الكمية المطلوب صرفها أكبر من الرصيد المتاح"))
        }

        itemDao().updateStock(transaction.itemId, newStock)
        val id = transactionDao().insertRaw(transaction)
        return Result.success(id)
    }
}

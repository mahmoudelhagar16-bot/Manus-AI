package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.farm.layermanager.data.local.entity.FeedConsumptionEntity
import com.farm.layermanager.data.local.entity.FeedTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedTypeDao {
    @Insert
    suspend fun insert(feedType: FeedTypeEntity): Long

    @Update
    suspend fun update(feedType: FeedTypeEntity)

    @Query("SELECT * FROM feed_types ORDER BY purchaseDate DESC")
    fun getAll(): Flow<List<FeedTypeEntity>>

    @Query("SELECT * FROM feed_types WHERE feedTypeId = :feedTypeId")
    suspend fun getByIdOnce(feedTypeId: Long): FeedTypeEntity?

    @Query("SELECT * FROM feed_types WHERE feedTypeId = :feedTypeId")
    fun getById(feedTypeId: Long): Flow<FeedTypeEntity?>

    @Query("UPDATE feed_types SET currentStockKg = :newStock, weightedAvgCost = :newAvgCost WHERE feedTypeId = :feedTypeId")
    suspend fun updateStockAndCost(feedTypeId: Long, newStock: Double, newAvgCost: Double)

    @Query("UPDATE feed_types SET currentStockKg = currentStockKg - :quantity WHERE feedTypeId = :feedTypeId")
    suspend fun deductStock(feedTypeId: Long, quantity: Double)
}

@Dao
interface FeedConsumptionDao {

    @Insert
    suspend fun insertConsumptionRaw(consumption: FeedConsumptionEntity): Long

    @Query("SELECT * FROM feed_consumption WHERE houseId = :houseId AND strainId = :strainId ORDER BY cDate DESC")
    fun getByStrain(houseId: Long, strainId: Long): Flow<List<FeedConsumptionEntity>>

    @Query("SELECT * FROM feed_consumption ORDER BY cDate DESC")
    fun getAll(): Flow<List<FeedConsumptionEntity>>

    @Query(
        """
        SELECT IFNULL(SUM(totalWeightKg), 0) FROM feed_consumption
        WHERE strainId = :strainId AND cDate BETWEEN :startDate AND :endDate
        """
    )
    fun getTotalWeightForStrainInRange(strainId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query(
        """
        SELECT IFNULL(SUM(cost), 0) FROM feed_consumption
        WHERE strainId = :strainId AND cDate BETWEEN :startDate AND :endDate
        """
    )
    fun getTotalCostForStrainInRange(strainId: Long, startDate: Long, endDate: Long): Flow<Double>
}

/**
 * DAO مركّب يضمّ العملية الذرية (خصم مخزون + إدراج سجل استهلاك) في @Transaction واحدة،
 * تماشياً مع مبدأ التدفق (قسم 6): "All-or-Nothing".
 * التحقق من (totalWeightKg <= currentStockKg) يجب أن يتم قبل استدعاء هذه الدالة في الـ UseCase،
 * لكن يُعاد التحقق هنا أيضاً كخط دفاع أخير قبل الكتابة الفعلية.
 */
@Dao
abstract class FeedConsumptionTransactionDao {

    protected abstract fun feedTypeDao(): FeedTypeDao
    protected abstract fun feedConsumptionDao(): FeedConsumptionDao

    @Transaction
    open suspend fun recordConsumption(consumption: FeedConsumptionEntity): Result<Long> {
        val feedType = feedTypeDao().getByIdOnce(consumption.feedTypeId)
            ?: return Result.failure(IllegalStateException("نوع العلف غير موجود"))

        if (consumption.totalWeightKg > feedType.currentStockKg) {
            return Result.failure(IllegalStateException("الكمية المطلوبة أكبر من الرصيد المتاح في المخزون"))
        }

        feedTypeDao().deductStock(consumption.feedTypeId, consumption.totalWeightKg)
        val id = feedConsumptionDao().insertConsumptionRaw(consumption)
        return Result.success(id)
    }

    /**
     * عملية وارد جديدة لعلف: تحديث المتوسط المرجّح للتكلفة وفق المعادلة:
     * newAvgCost = ((oldStock × oldAvgCost) + (newQty × newPrice)) / (oldStock + newQty)
     */
    @Transaction
    open suspend fun recordFeedPurchase(feedTypeId: Long, newQtyKg: Double, newPrice: Double): Result<Unit> {
        val feedType = feedTypeDao().getByIdOnce(feedTypeId)
            ?: return Result.failure(IllegalStateException("نوع العلف غير موجود"))

        val oldStock = feedType.currentStockKg
        val oldAvgCost = feedType.weightedAvgCost
        val newStock = oldStock + newQtyKg
        val newAvgCost = if (newStock > 0) {
            ((oldStock * oldAvgCost) + (newQtyKg * newPrice)) / newStock
        } else {
            newPrice
        }

        feedTypeDao().updateStockAndCost(feedTypeId, newStock, newAvgCost)
        return Result.success(Unit)
    }
}

package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.farm.layermanager.data.local.entity.ExpenseCategoryEntity
import com.farm.layermanager.data.local.entity.ExpenseEntity
import com.farm.layermanager.data.local.entity.RevenueEntity
import com.farm.layermanager.data.local.entity.RevenueTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {
    @Insert
    suspend fun insert(category: ExpenseCategoryEntity): Long

    @Query("SELECT * FROM expense_categories ORDER BY categoryName")
    fun getAll(): Flow<List<ExpenseCategoryEntity>>
}

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE eDate BETWEEN :startDate AND :endDate ORDER BY eDate DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE houseId IS NULL AND eDate BETWEEN :startDate AND :endDate")
    fun getUnallocatedInRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE houseId = :houseId AND eDate BETWEEN :startDate AND :endDate")
    fun getByHouseInRange(houseId: Long, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM expenses WHERE eDate BETWEEN :startDate AND :endDate")
    fun getTotalInRange(startDate: Long, endDate: Long): Flow<Double>

    @Query(
        """
        SELECT ec.categoryName AS categoryName, IFNULL(SUM(e.amount), 0) AS total
        FROM expense_categories ec
        LEFT JOIN expenses e ON e.categoryId = ec.categoryId AND e.eDate BETWEEN :startDate AND :endDate
        GROUP BY ec.categoryId
        """
    )
    fun getTotalsByCategory(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>
}

data class CategoryTotal(val categoryName: String, val total: Double)

@Dao
interface RevenueTypeDao {
    @Insert
    suspend fun insert(type: RevenueTypeEntity): Long

    @Query("SELECT * FROM revenue_types ORDER BY typeName")
    fun getAll(): Flow<List<RevenueTypeEntity>>
}

@Dao
interface RevenueDao {
    @Insert
    suspend fun insert(revenue: RevenueEntity): Long

    @Update
    suspend fun update(revenue: RevenueEntity)

    @Query("SELECT * FROM revenues WHERE rDate BETWEEN :startDate AND :endDate ORDER BY rDate DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<RevenueEntity>>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM revenues WHERE rDate BETWEEN :startDate AND :endDate")
    fun getTotalInRange(startDate: Long, endDate: Long): Flow<Double>
}

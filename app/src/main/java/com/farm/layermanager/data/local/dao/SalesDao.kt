package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.farm.layermanager.data.local.entity.CustomerEntity
import com.farm.layermanager.data.local.entity.EggInventoryBalanceView
import com.farm.layermanager.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE customerId = :customerId")
    fun getById(customerId: Long): Flow<CustomerEntity?>

    /** إجمالي المديونية (المتبقي) على عميل معيّن — كشف حساب */
    @Query("SELECT IFNULL(SUM(remainingAmount), 0) FROM sales WHERE customerId = :customerId")
    fun getTotalDebtForCustomer(customerId: Long): Flow<Double>
}

@Dao
interface SaleDao {
    @Insert
    suspend fun insert(sale: SaleEntity): Long

    @Update
    suspend fun update(sale: SaleEntity)

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY sDate DESC")
    fun getByCustomer(customerId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY sDate DESC")
    fun getAll(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE sDate BETWEEN :startDate AND :endDate ORDER BY sDate DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM egg_inventory_balance LIMIT 1")
    suspend fun getEggInventoryBalanceOnce(): EggInventoryBalanceView?

    @Query("SELECT * FROM egg_inventory_balance LIMIT 1")
    fun getEggInventoryBalance(): Flow<EggInventoryBalanceView?>

    @Query("SELECT IFNULL(SUM(totalAmount), 0) FROM sales WHERE sDate BETWEEN :startDate AND :endDate")
    fun getTotalRevenueInRange(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT IFNULL(SUM(remainingAmount), 0) FROM sales")
    fun getTotalOutstandingDebt(): Flow<Double>
}

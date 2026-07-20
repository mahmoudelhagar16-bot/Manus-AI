package com.farm.layermanager.domain.repository

import com.farm.layermanager.domain.model.Customer
import com.farm.layermanager.domain.model.Sale
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CustomerRepository {
    suspend fun insert(customer: Customer): Long
    suspend fun update(customer: Customer)
    fun getAll(): Flow<List<Customer>>
    fun getById(customerId: Long): Flow<Customer?>
    fun getTotalDebtForCustomer(customerId: Long): Flow<Double>
}

/** يحمل الرصيد المتاح من البيض غير المباع (View: egg_inventory_balance، قسم 12). */
data class EggInventoryBalance(
    val totalProducedTrays: Double,
    val totalSoldTrays: Double,
    val availableTrays: Double
)

interface SaleRepository {
    suspend fun insert(sale: Sale): Long
    suspend fun update(sale: Sale)
    fun getByCustomer(customerId: Long): Flow<List<Sale>>
    fun getAll(): Flow<List<Sale>>
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Sale>>
    suspend fun getEggInventoryBalanceOnce(): EggInventoryBalance?
    fun getEggInventoryBalance(): Flow<EggInventoryBalance?>
    fun getTotalRevenueInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double>
    fun getTotalOutstandingDebt(): Flow<Double>
}

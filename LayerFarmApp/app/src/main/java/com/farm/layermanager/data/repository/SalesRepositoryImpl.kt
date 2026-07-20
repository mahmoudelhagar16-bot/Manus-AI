package com.farm.layermanager.data.repository

import com.farm.layermanager.data.local.dao.CustomerDao
import com.farm.layermanager.data.local.dao.SaleDao
import com.farm.layermanager.data.mapper.toDomain
import com.farm.layermanager.data.mapper.toEntity
import com.farm.layermanager.domain.model.Customer
import com.farm.layermanager.domain.model.Sale
import com.farm.layermanager.domain.repository.CustomerRepository
import com.farm.layermanager.domain.repository.EggInventoryBalance
import com.farm.layermanager.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val dao: CustomerDao
) : CustomerRepository {

    override suspend fun insert(customer: Customer): Long = dao.insert(customer.toEntity())

    override suspend fun update(customer: Customer) = dao.update(customer.toEntity())

    override fun getAll(): Flow<List<Customer>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(customerId: Long): Flow<Customer?> = dao.getById(customerId).map { it?.toDomain() }

    override fun getTotalDebtForCustomer(customerId: Long): Flow<Double> = dao.getTotalDebtForCustomer(customerId)
}

class SaleRepositoryImpl @Inject constructor(
    private val dao: SaleDao
) : SaleRepository {

    override suspend fun insert(sale: Sale): Long = dao.insert(sale.toEntity())

    override suspend fun update(sale: Sale) = dao.update(sale.toEntity())

    override fun getByCustomer(customerId: Long): Flow<List<Sale>> =
        dao.getByCustomer(customerId).map { list -> list.map { it.toDomain() } }

    override fun getAll(): Flow<List<Sale>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Sale>> =
        dao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override suspend fun getEggInventoryBalanceOnce(): EggInventoryBalance? =
        dao.getEggInventoryBalanceOnce()?.toDomain()

    override fun getEggInventoryBalance(): Flow<EggInventoryBalance?> =
        dao.getEggInventoryBalance().map { it?.toDomain() }

    override fun getTotalRevenueInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        dao.getTotalRevenueInRange(startDate.toEpochDay(), endDate.toEpochDay())

    override fun getTotalOutstandingDebt(): Flow<Double> = dao.getTotalOutstandingDebt()
}

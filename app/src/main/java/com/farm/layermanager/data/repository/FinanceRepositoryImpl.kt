package com.farm.layermanager.data.repository

import com.farm.layermanager.data.local.dao.ExpenseCategoryDao
import com.farm.layermanager.data.local.dao.ExpenseDao
import com.farm.layermanager.data.local.dao.RevenueDao
import com.farm.layermanager.data.local.dao.RevenueTypeDao
import com.farm.layermanager.data.mapper.toDomain
import com.farm.layermanager.data.mapper.toEntity
import com.farm.layermanager.domain.model.Expense
import com.farm.layermanager.domain.model.ExpenseCategory
import com.farm.layermanager.domain.model.Revenue
import com.farm.layermanager.domain.model.RevenueType
import com.farm.layermanager.domain.repository.CategoryTotal
import com.farm.layermanager.domain.repository.ExpenseRepository
import com.farm.layermanager.domain.repository.RevenueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val categoryDao: ExpenseCategoryDao,
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun insertCategory(category: ExpenseCategory): Long = categoryDao.insert(category.toEntity())

    override fun getCategories(): Flow<List<ExpenseCategory>> =
        categoryDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(expense: Expense): Long = expenseDao.insert(expense.toEntity())

    override suspend fun update(expense: Expense) = expenseDao.update(expense.toEntity())

    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> =
        expenseDao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun getUnallocatedInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> =
        expenseDao.getUnallocatedInRange(startDate.toEpochDay(), endDate.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun getByHouseInRange(houseId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> =
        expenseDao.getByHouseInRange(houseId, startDate.toEpochDay(), endDate.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun getTotalInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        expenseDao.getTotalInRange(startDate.toEpochDay(), endDate.toEpochDay())

    override fun getTotalsByCategory(startDate: LocalDate, endDate: LocalDate): Flow<List<CategoryTotal>> =
        expenseDao.getTotalsByCategory(startDate.toEpochDay(), endDate.toEpochDay()).map { list -> list.map { it.toDomain() } }
}

class RevenueRepositoryImpl @Inject constructor(
    private val typeDao: RevenueTypeDao,
    private val revenueDao: RevenueDao
) : RevenueRepository {

    override suspend fun insertType(type: RevenueType): Long = typeDao.insert(type.toEntity())

    override fun getTypes(): Flow<List<RevenueType>> = typeDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(revenue: Revenue): Long = revenueDao.insert(revenue.toEntity())

    override suspend fun update(revenue: Revenue) = revenueDao.update(revenue.toEntity())

    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Revenue>> =
        revenueDao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun getTotalInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        revenueDao.getTotalInRange(startDate.toEpochDay(), endDate.toEpochDay())
}

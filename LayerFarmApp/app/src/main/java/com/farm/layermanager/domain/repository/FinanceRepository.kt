package com.farm.layermanager.domain.repository

import com.farm.layermanager.domain.model.Expense
import com.farm.layermanager.domain.model.ExpenseCategory
import com.farm.layermanager.domain.model.Revenue
import com.farm.layermanager.domain.model.RevenueType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class CategoryTotal(val categoryName: String, val total: Double)

interface ExpenseRepository {
    suspend fun insertCategory(category: ExpenseCategory): Long
    fun getCategories(): Flow<List<ExpenseCategory>>

    suspend fun insert(expense: Expense): Long
    suspend fun update(expense: Expense)
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getUnallocatedInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getByHouseInRange(houseId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getTotalInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double>
    fun getTotalsByCategory(startDate: LocalDate, endDate: LocalDate): Flow<List<CategoryTotal>>
}

interface RevenueRepository {
    suspend fun insertType(type: RevenueType): Long
    fun getTypes(): Flow<List<RevenueType>>

    suspend fun insert(revenue: Revenue): Long
    suspend fun update(revenue: Revenue)
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Revenue>>
    fun getTotalInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double>
}

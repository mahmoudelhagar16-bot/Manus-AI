package com.farm.layermanager.domain.usecase.finance

import com.farm.layermanager.domain.calculation.CalculationEngine
import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.common.Validator
import com.farm.layermanager.domain.model.Expense
import com.farm.layermanager.domain.model.Revenue
import com.farm.layermanager.domain.repository.ExpenseRepository
import com.farm.layermanager.domain.repository.RevenueRepository
import com.farm.layermanager.domain.repository.SaleRepository
import com.farm.layermanager.domain.repository.StrainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** UC-15 */
class AddExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: Expense): DomainResult<Long> {
        Validator.requirePositive(expense.amount, "المبلغ")?.let { return DomainResult.Error(it) }

        val id = repository.insert(expense)
        return DomainResult.Success(id)
    }
}

/** UC-16 */
class AddRevenueUseCase(private val repository: RevenueRepository) {
    suspend operator fun invoke(revenue: Revenue): DomainResult<Long> {
        Validator.requirePositive(revenue.amount, "المبلغ")?.let { return DomainResult.Error(it) }

        val id = repository.insert(revenue)
        return DomainResult.Success(id)
    }
}

class GetExpenseCategoriesUseCase(private val repository: ExpenseRepository) {
    operator fun invoke() = repository.getCategories()
}

/**
 * توزيع مصروف عام (houseId = null) على عنبر محدد حسب نسبة طيوره من إجمالي طيور المزرعة.
 * يُرجع null إن كان إجمالي الطيور صفراً (يبقى المصروف "غير موزَّع" — قسم 14).
 */
class AllocateGeneralExpenseUseCase(private val strainRepository: StrainRepository) {
    suspend operator fun invoke(generalExpenseAmount: Double, houseId: Long): Double? {
        val strains = strainRepository.getAll().first()
        val statsById = strainRepository.getAllCumulativeStats().first().associateBy { it.strainId }

        val totalFarmBirds = strains.sumOf { statsById[it.strainId]?.currentBirds ?: 0 }
        val houseBirds = strains
            .filter { it.houseId == houseId }
            .sumOf { statsById[it.strainId]?.currentBirds ?: 0 }

        return CalculationEngine.allocateGeneralExpenseToHouse(generalExpenseAmount, houseBirds, totalFarmBirds)
    }

    /** يُرجع خريطة (houseId -> نصيبه من المصروف) لكل العنابر النشطة دفعة واحدة — مفيد لتقرير شامل. */
    suspend fun allocateToAllHouses(generalExpenseAmount: Double): Map<Long, Double?> {
        val strains = strainRepository.getAll().first()
        val statsById = strainRepository.getAllCumulativeStats().first().associateBy { it.strainId }
        val birdsByHouse = strains
            .groupBy { it.houseId }
            .mapValues { (_, list) -> list.sumOf { statsById[it.strainId]?.currentBirds ?: 0 } }

        val totalFarmBirds = birdsByHouse.values.sum()
        return birdsByHouse.mapValues { (_, houseBirds) ->
            CalculationEngine.allocateGeneralExpenseToHouse(generalExpenseAmount, houseBirds, totalFarmBirds)
        }
    }
}

class GetFinanceUseCase(
    private val expenseRepository: ExpenseRepository,
    private val revenueRepository: RevenueRepository
) {
    fun getExpensesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> =
        expenseRepository.getByDateRange(startDate, endDate)

    fun getRevenuesInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Revenue>> =
        revenueRepository.getByDateRange(startDate, endDate)

    fun getTotalExpensesInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        expenseRepository.getTotalInRange(startDate, endDate)

    fun getTotalOtherRevenueInRange(startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        revenueRepository.getTotalInRange(startDate, endDate)
}

/**
 * UC-17: صافي الربح لفترة مختارة = (مبيعات + إيرادات أخرى) − مصروفات، عبر CalculationEngine.
 */
class GetNetProfitUseCase(
    private val saleRepository: SaleRepository,
    private val expenseRepository: ExpenseRepository,
    private val revenueRepository: RevenueRepository
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        combine(
            saleRepository.getTotalRevenueInRange(startDate, endDate),
            revenueRepository.getTotalInRange(startDate, endDate),
            expenseRepository.getTotalInRange(startDate, endDate)
        ) { salesRevenue, otherRevenue, expenses ->
            CalculationEngine.netProfit(salesRevenue, otherRevenue, expenses)
        }
}

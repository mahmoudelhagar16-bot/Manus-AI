package com.farm.layermanager.domain.usecase.reports

import com.farm.layermanager.domain.calculation.CalculationEngine
import com.farm.layermanager.domain.repository.DailyRecordRepository
import com.farm.layermanager.domain.repository.ExpenseRepository
import com.farm.layermanager.domain.repository.FeedRepository
import com.farm.layermanager.domain.repository.MedicationRepository
import com.farm.layermanager.domain.repository.RevenueRepository
import com.farm.layermanager.domain.repository.SaleRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** نطاق التقرير: عنبر+سلالة محددة، أو عنبر كامل (كل سلالاته)، أو المزرعة بالكامل. */
sealed class ReportScope {
    data class HouseStrain(val houseId: Long, val strainId: Long) : ReportScope()
    data class WholeHouse(val houseId: Long) : ReportScope()
    object WholeFarm : ReportScope()
}

data class PeriodReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalMortality: Int,
    val totalCulled: Int,
    val totalProductionTrays: Double,
    val averageHenDayPercent: Double,
    val totalFeedConsumedKg: Double,
    val fcr: Double,
    val totalFeedCost: Double,
    val totalHealthCost: Double,
    val totalSalesRevenue: Double,
    val totalOtherRevenue: Double,
    val totalExpenses: Double,
    val netProfit: Double
)

/**
 * UC-19: توليد تقرير شامل لفترة زمنية ونطاق محددين.
 * ملاحظة: التقرير على مستوى (عنبر × سلالة) هو الأدق لأنه يستخدم بيانات daily_records/feed_consumption
 * المرتبطة مباشرة بـ (houseId, strainId). أما WholeHouse/WholeFarm فيُجمِّعان بيانات المبيعات/المصروفات
 * العامة على مستوى المزرعة (لأن sales/expenses ليست دائماً مرتبطة بسلالة بعينها في هذا المخطط).
 */
class GenerateReportUseCase(
    private val dailyRecordRepository: DailyRecordRepository,
    private val feedRepository: FeedRepository,
    private val medicationRepository: MedicationRepository,
    private val saleRepository: SaleRepository,
    private val expenseRepository: ExpenseRepository,
    private val revenueRepository: RevenueRepository
) {
    suspend operator fun invoke(
        scope: ReportScope,
        startDate: LocalDate,
        endDate: LocalDate,
        avgEggWeightGrams: Double = 60.0
    ): PeriodReport {
        val records = when (scope) {
            is ReportScope.HouseStrain ->
                dailyRecordRepository.getByStrainAndDateRange(scope.houseId, scope.strainId, startDate, endDate).first()
            is ReportScope.WholeHouse, ReportScope.WholeFarm ->
                dailyRecordRepository.getByDateRange(startDate, endDate).first()
        }

        val totalMortality = records.sumOf { it.mortality }
        val totalCulled = records.sumOf { it.culled }
        val totalProductionTrays = records.sumOf { it.productionTrays }
        val totalEggs = records.sumOf { CalculationEngine.producedEggsCount(it.productionTrays) }

        val henDayValues = records
            .filter { it.liveBirds > 0 }
            .map { CalculationEngine.henDayPercent(it.productionTrays, it.liveBirds) }
        val avgHenDay = CalculationEngine.averageHenDayForPeriod(henDayValues)

        val totalFeedKg = when (scope) {
            is ReportScope.HouseStrain ->
                feedRepository.getConsumptionTotalWeightForStrainInRange(scope.strainId, startDate, endDate).first()
            else -> records.sumOf { it.feedQtyKg }
        }
        val totalFeedCost = when (scope) {
            is ReportScope.HouseStrain ->
                feedRepository.getConsumptionTotalCostForStrainInRange(scope.strainId, startDate, endDate).first()
            else -> 0.0
        }
        val totalHealthCost = when (scope) {
            is ReportScope.HouseStrain ->
                medicationRepository.getTotalCostForStrainInRange(scope.strainId, startDate, endDate).first()
            else -> 0.0
        }

        val fcr = CalculationEngine.fcr(totalFeedKg, totalEggs, avgEggWeightGrams)

        val totalSalesRevenue = saleRepository.getTotalRevenueInRange(startDate, endDate).first()
        val totalOtherRevenue = revenueRepository.getTotalInRange(startDate, endDate).first()
        val totalExpenses = expenseRepository.getTotalInRange(startDate, endDate).first()

        return PeriodReport(
            startDate = startDate,
            endDate = endDate,
            totalMortality = totalMortality,
            totalCulled = totalCulled,
            totalProductionTrays = totalProductionTrays,
            averageHenDayPercent = avgHenDay,
            totalFeedConsumedKg = totalFeedKg,
            fcr = fcr,
            totalFeedCost = totalFeedCost,
            totalHealthCost = totalHealthCost,
            totalSalesRevenue = totalSalesRevenue,
            totalOtherRevenue = totalOtherRevenue,
            totalExpenses = totalExpenses,
            netProfit = CalculationEngine.netProfit(totalSalesRevenue, totalOtherRevenue, totalExpenses)
        )
    }
}

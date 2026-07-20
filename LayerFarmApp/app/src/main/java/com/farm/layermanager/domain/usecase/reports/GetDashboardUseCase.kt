package com.farm.layermanager.domain.usecase.reports

import com.farm.layermanager.domain.calculation.CalculationEngine
import com.farm.layermanager.domain.model.DailyRecord
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.Sale
import com.farm.layermanager.domain.repository.DailyRecordRepository
import com.farm.layermanager.domain.repository.ExpenseRepository
import com.farm.layermanager.domain.repository.HouseRepository
import com.farm.layermanager.domain.repository.RevenueRepository
import com.farm.layermanager.domain.repository.SaleRepository
import com.farm.layermanager.domain.repository.StrainCumulativeStats
import com.farm.layermanager.domain.repository.StrainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

data class DashboardSnapshot(
    val activeHousesCount: Int,
    val currentBirdCount: Int,
    val mortalityToday: Int,
    val productionToday: Double,
    val productionThisWeek: Double,
    val productionThisMonth: Double,
    val salesToday: Double,
    val salesThisMonth: Double,
    val totalRevenueThisMonth: Double,
    val totalExpensesThisMonth: Double,
    val netProfitThisMonth: Double,
    val henDayPercentToday: Double,
    val mortalityPercentCumulative: Double,
    val outstandingDebt: Double
)

/** تجميعة وسيطة لأول 5 مصادر بيانات — تُستخدم لتفادي تجاوز حد الـ combine الآمن نوعياً (5 Flows). */
private data class HerdAndProductionPart(
    val activeHouses: List<House>,
    val strainStats: List<StrainCumulativeStats>,
    val todayRecords: List<DailyRecord>,
    val weekRecords: List<DailyRecord>,
    val monthRecords: List<DailyRecord>
)

/** تجميعة وسيطة للمصادر المالية والمبيعات. */
private data class FinanceAndSalesPart(
    val todaySales: List<Sale>,
    val monthSalesRevenue: Double,
    val monthOtherRevenue: Double,
    val monthExpenses: Double,
    val outstandingDebt: Double
)

/**
 * UC-18: يبني لقطة Dashboard كاملة عند الطلب، بدمج عدة مصادر عبر Flow.combine
 * (يبقى محدَّثاً لحظياً تلقائياً لأن كل Repository يعتمد على Flow من Room).
 * مقسَّم إلى مرحلتين لتفادي تجاوز حد overloads النوعية الآمنة لـ combine (5 Flows كحد أقصى لكل استدعاء).
 */
class GetDashboardUseCase(
    private val houseRepository: HouseRepository,
    private val strainRepository: StrainRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val saleRepository: SaleRepository,
    private val expenseRepository: ExpenseRepository,
    private val revenueRepository: RevenueRepository
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<DashboardSnapshot> {
        val weekStart = today.minusDays(6)
        val monthStart = today.withDayOfMonth(1)

        val herdAndProductionFlow: Flow<HerdAndProductionPart> = combine(
            houseRepository.getActive(),
            strainRepository.getAllCumulativeStats(),
            dailyRecordRepository.getByDateRange(today, today),
            dailyRecordRepository.getByDateRange(weekStart, today),
            dailyRecordRepository.getByDateRange(monthStart, today)
        ) { activeHouses, strainStats, todayRecords, weekRecords, monthRecords ->
            HerdAndProductionPart(activeHouses, strainStats, todayRecords, weekRecords, monthRecords)
        }

        val financeAndSalesFlow: Flow<FinanceAndSalesPart> = combine(
            saleRepository.getByDateRange(today, today),
            saleRepository.getTotalRevenueInRange(monthStart, today),
            revenueRepository.getTotalInRange(monthStart, today),
            expenseRepository.getTotalInRange(monthStart, today),
            saleRepository.getTotalOutstandingDebt()
        ) { todaySales, monthSalesRevenue, monthOtherRevenue, monthExpenses, outstandingDebt ->
            FinanceAndSalesPart(todaySales, monthSalesRevenue, monthOtherRevenue, monthExpenses, outstandingDebt)
        }

        return combine(herdAndProductionFlow, financeAndSalesFlow) { herd, finance ->
            val currentBirds = herd.strainStats.sumOf { it.currentBirds }
            val initialTotal = herd.strainStats.sumOf { it.initialChickCount }
            val totalMortality = herd.strainStats.sumOf { it.totalMortality }

            val mortalityToday = herd.todayRecords.sumOf { it.mortality }
            val productionToday = herd.todayRecords.sumOf { it.productionTrays }
            val productionThisWeek = herd.weekRecords.sumOf { it.productionTrays }
            val productionThisMonth = herd.monthRecords.sumOf { it.productionTrays }
            val liveBirdsToday = herd.todayRecords.sumOf { it.liveBirds }

            DashboardSnapshot(
                activeHousesCount = herd.activeHouses.size,
                currentBirdCount = currentBirds,
                mortalityToday = mortalityToday,
                productionToday = productionToday,
                productionThisWeek = productionThisWeek,
                productionThisMonth = productionThisMonth,
                salesToday = finance.todaySales.sumOf { it.totalAmount },
                salesThisMonth = finance.monthSalesRevenue,
                totalRevenueThisMonth = finance.monthSalesRevenue + finance.monthOtherRevenue,
                totalExpensesThisMonth = finance.monthExpenses,
                netProfitThisMonth = CalculationEngine.netProfit(
                    finance.monthSalesRevenue, finance.monthOtherRevenue, finance.monthExpenses
                ),
                henDayPercentToday = if (liveBirdsToday > 0)
                    CalculationEngine.henDayPercent(productionToday, liveBirdsToday) else 0.0,
                mortalityPercentCumulative = if (initialTotal > 0)
                    CalculationEngine.mortalityPercent(totalMortality, initialTotal) else 0.0,
                outstandingDebt = finance.outstandingDebt
            )
        }
    }
}

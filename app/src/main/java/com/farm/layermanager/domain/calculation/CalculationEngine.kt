package com.farm.layermanager.domain.calculation

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * كل الدوال هنا نقية (Pure Functions): لا I/O ولا قواعد بيانات، فقط أرقام تدخل وأرقام تخرج.
 * هذا يجعلها قابلة للاختبار الوحدوي (Unit Tests) بمعزل تام عن Room/Compose (قسم 8).
 * كل دالة تقابل صفاً واحداً بالضبط من جدول المعادلات في قسم 12 من وثيقة التحليل.
 */
object CalculationEngine {

    // ---------- 1. العمر والبقاء ----------

    /** العمر الحالي بالأسابيع = (تاريخ اليوم − تاريخ الاستقبال) ÷ 7 */
    fun currentAgeWeeks(arrivalDate: LocalDate, today: LocalDate = LocalDate.now()): Long {
        val days = ChronoUnit.DAYS.between(arrivalDate, today).coerceAtLeast(0)
        return days / 7
    }

    /** عدد الطيور الحالية = initialChickCount − Σmortality − Σculled */
    fun currentBirdCount(initialChickCount: Int, cumulativeMortality: Int, cumulativeCulled: Int): Int =
        (initialChickCount - cumulativeMortality - cumulativeCulled).coerceAtLeast(0)

    /** نسبة البقاء % = (عدد الطيور الحالية ÷ initialChickCount) × 100 */
    fun livabilityPercent(currentBirdCount: Int, initialChickCount: Int): Double =
        safeDivide(currentBirdCount.toDouble(), initialChickCount.toDouble()) * 100.0

    /** معدل النفوق % = (Σmortality ÷ initialChickCount) × 100 */
    fun mortalityPercent(cumulativeMortality: Int, initialChickCount: Int): Double =
        safeDivide(cumulativeMortality.toDouble(), initialChickCount.toDouble()) * 100.0

    // ---------- 2. الإنتاج ----------

    /** عدد البيض المنتج = productionTrays × عدد البيض بالطبق (افتراضي 30، قابل للتعديل من الإعدادات) */
    fun producedEggsCount(productionTrays: Double, eggsPerTray: Int = 30): Int =
        (productionTrays * eggsPerTray).roundToInt()

    /** نسبة الإنتاج اليومي (Hen-Day %) = (عدد البيض المنتج اليوم ÷ عدد الطيور الحية اليوم) × 100 */
    fun henDayPercent(productionTrays: Double, liveBirds: Int, eggsPerTray: Int = 30): Double {
        val eggs = producedEggsCount(productionTrays, eggsPerTray)
        return safeDivide(eggs.toDouble(), liveBirds.toDouble()) * 100.0
    }

    /** متوسط الإنتاج لفترة = متوسط Hen-Day % خلال الفترة */
    fun averageHenDayForPeriod(henDayPercentsPerDay: List<Double>): Double =
        if (henDayPercentsPerDay.isEmpty()) 0.0 else henDayPercentsPerDay.average()

    // ---------- 3. العلف ----------

    /**
     * معامل التحويل الغذائي FCR = إجمالي العلف المستهلك (kg) ÷ إجمالي وزن البيض المنتج (kg)
     * وزن البيضة التقديري = 60 جم افتراضياً (قابل للتعديل من الإعدادات).
     */
    fun fcr(totalFeedConsumedKg: Double, totalEggsProduced: Int, avgEggWeightGrams: Double = 60.0): Double {
        val totalEggWeightKg = (totalEggsProduced * avgEggWeightGrams) / 1000.0
        return safeDivide(totalFeedConsumedKg, totalEggWeightKg)
    }

    /** متوسط استهلاك العلف للطائر/اليوم = إجمالي العلف اليومي (kg) ÷ عدد الطيور الحية */
    fun avgFeedConsumptionPerBird(dailyFeedKg: Double, liveBirds: Int): Double =
        safeDivide(dailyFeedKg, liveBirds.toDouble())

    /**
     * متوسط التكلفة المرجّح للعلف عند وارد جديد:
     * newAvgCost = ((oldStock × oldAvgCost) + (newQty × newPrice)) / (oldStock + newQty)
     */
    fun weightedAverageFeedCost(
        oldStockKg: Double,
        oldAvgCost: Double,
        newQtyKg: Double,
        newPrice: Double
    ): Double {
        val newStock = oldStockKg + newQtyKg
        return if (newStock <= 0.0) newPrice
        else ((oldStockKg * oldAvgCost) + (newQtyKg * newPrice)) / newStock
    }

    // ---------- 4. التكاليف ----------

    /**
     * تكلفة الطبقة = (تكلفة العلف المخصصة + تكلفة الصحة المخصصة + حصة المصروفات العامة) للفترة
     *                 ÷ عدد الأطباق المنتجة
     */
    fun costPerTray(
        allocatedFeedCost: Double,
        allocatedHealthCost: Double,
        allocatedGeneralExpenseShare: Double,
        producedTrays: Double
    ): Double {
        val totalCost = allocatedFeedCost + allocatedHealthCost + allocatedGeneralExpenseShare
        return safeDivide(totalCost, producedTrays)
    }

    /** تكلفة البيضة = تكلفة الطبقة ÷ 30 */
    fun costPerEgg(costPerTray: Double, eggsPerTray: Int = 30): Double =
        costPerTray / eggsPerTray

    /** تكلفة الطائر التراكمية = إجمالي كل التكاليف منذ الاستقبال ÷ عدد الطيور المستلمة أصلاً */
    fun costPerBirdCumulative(totalCostSinceArrival: Double, initialChickCount: Int): Double =
        safeDivide(totalCostSinceArrival, initialChickCount.toDouble())

    // ---------- 5. المبيعات والربحية ----------

    /** متوسط سعر بيع الطبقة = إجمالي إيراد المبيعات ÷ إجمالي الأطباق المباعة (بكل الأنواع) */
    fun averageSalePricePerTray(totalSalesRevenue: Double, totalTraysSold: Double): Double =
        safeDivide(totalSalesRevenue, totalTraysSold)

    /** صافي الربح = إجمالي الإيرادات (مبيعات + إيرادات أخرى) − إجمالي المصروفات */
    fun netProfit(totalSalesRevenue: Double, totalOtherRevenue: Double, totalExpenses: Double): Double =
        (totalSalesRevenue + totalOtherRevenue) - totalExpenses

    /**
     * رصيد مخزون البيض غير المباع =
     *   إجمالي الأطباق المنتجة تراكمياً − إجمالي الأطباق المباعة تراكمياً − الهالك (إن اعتُبر غير قابل للبيع)
     */
    fun unsoldEggBalanceTrays(
        totalProducedTrays: Double,
        totalSoldTrays: Double,
        wastedTrays: Double = 0.0
    ): Double = (totalProducedTrays - totalSoldTrays - wastedTrays).coerceAtLeast(0.0)

    // ---------- 6. توزيع المصروفات العامة ----------

    /**
     * توزيع مصروف عام على عنبر = المصروف العام × (طيور العنبر ÷ إجمالي طيور المزرعة)
     * إذا كان إجمالي الطيور صفراً، يبقى المصروف "غير موزَّع" (null) بدل القسمة على صفر (قسم 14).
     */
    fun allocateGeneralExpenseToHouse(
        generalExpenseAmount: Double,
        houseCurrentBirds: Int,
        totalFarmBirds: Int
    ): Double? {
        if (totalFarmBirds <= 0) return null
        return generalExpenseAmount * (houseCurrentBirds.toDouble() / totalFarmBirds.toDouble())
    }

    // ---------- أداة مساعدة داخلية ----------

    /** قسمة آمنة تُرجع صفراً بدل NaN/Infinity عند القسمة على صفر (يمنع أعطال الواجهة). */
    private fun safeDivide(numerator: Double, denominator: Double): Double =
        if (denominator == 0.0) 0.0 else numerator / denominator
}

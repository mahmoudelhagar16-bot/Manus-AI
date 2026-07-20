package com.farm.layermanager.data.local.entity

import androidx.room.DatabaseView

/**
 * رصيد مخزون البيض غير المباع لكل سلالة:
 * إجمالي الأطباق المنتجة تراكمياً − إجمالي الأطباق المباعة تراكمياً.
 * ملاحظة: ربط المبيعات بالسلالة غير موجود في المخطط الحالي (المبيعات على مستوى المزرعة/العميل
 * وليست موزَّعة على سلالة بعينها)، لذا الرصيد الإجمالي على مستوى المزرعة هو الأدق فعلياً.
 * هذا الـ View يقدّم الرصيد الإجمالي على مستوى المزرعة بالكامل.
 */
@DatabaseView(
    viewName = "egg_inventory_balance",
    value = """
        SELECT
            (SELECT IFNULL(SUM(productionTrays), 0) FROM daily_records) AS totalProducedTrays,
            (SELECT IFNULL(SUM(whiteTrays + redTrays + crackedTrays), 0) FROM sales) AS totalSoldTrays,
            (
                (SELECT IFNULL(SUM(productionTrays), 0) FROM daily_records)
                -
                (SELECT IFNULL(SUM(whiteTrays + redTrays + crackedTrays), 0) FROM sales)
            ) AS availableTrays
    """
)
data class EggInventoryBalanceView(
    val totalProducedTrays: Double,
    val totalSoldTrays: Double,
    val availableTrays: Double
)

/**
 * مؤشرات السلالة المشتقة الأساسية (تراكمي حتى اليوم)، تُبنى فوق daily_records فقط.
 * الحسابات الأدق (نسبة بقاء، Hen-Day، FCR...) تبقى في CalculationEngine بالـ Domain
 * لأنها تحتاج تواريخ/فترات مرنة لا يوفرها SQL بسهولة، لكن هذا الـ View يفيد في القوائم السريعة.
 */
@DatabaseView(
    viewName = "strain_cumulative_stats",
    value = """
        SELECT
            s.strainId AS strainId,
            s.initialChickCount AS initialChickCount,
            IFNULL(SUM(dr.mortality), 0) AS totalMortality,
            IFNULL(SUM(dr.culled), 0) AS totalCulled,
            (s.initialChickCount - IFNULL(SUM(dr.mortality), 0) - IFNULL(SUM(dr.culled), 0)) AS currentBirds
        FROM strains s
        LEFT JOIN daily_records dr ON dr.strainId = s.strainId
        GROUP BY s.strainId
    """
)
data class StrainCumulativeStatsView(
    val strainId: Long,
    val initialChickCount: Int,
    val totalMortality: Int,
    val totalCulled: Int,
    val currentBirds: Int
)

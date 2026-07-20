package com.farm.layermanager.ui.navigation

/**
 * قائمة مبدئية بالمسارات — تُستكمل تباعاً مع كل شاشة جديدة (علف/مخزون/مبيعات/مالية/Dashboard/تقارير).
 */
sealed class Screen(val route: String) {
    object HouseList : Screen("house_list")
    object StrainList : Screen("house/{houseId}/strains") {
        fun createRoute(houseId: Long) = "house/$houseId/strains"
    }
    object DailyRecordEntry : Screen("house/{houseId}/strain/{strainId}/daily_record") {
        fun createRoute(houseId: Long, strainId: Long) = "house/$houseId/strain/$strainId/daily_record"
    }
    object Health : Screen("house/{houseId}/strain/{strainId}/health") {
        fun createRoute(houseId: Long, strainId: Long) = "house/$houseId/strain/$strainId/health"
    }
    object Reports : Screen("reports")

    // ---------- وجهات شريط التنقل السفلي الرئيسية ----------
    object Dashboard : Screen("dashboard")
    object Feed : Screen("feed")
    object Inventory : Screen("inventory")
    object Sales : Screen("sales")
    object Finance : Screen("finance")

    companion object {
        const val ARG_HOUSE_ID = "houseId"
        const val ARG_STRAIN_ID = "strainId"

        /** الوجهات الست الظاهرة في شريط التنقل السفلي (راجع bottomBarItems الفعلية في LayerFarmNavHost.kt). */
        val bottomBarScreens = listOf(Dashboard, HouseList, Feed, Inventory, Sales, Finance)
    }
}

package com.farm.layermanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.farm.layermanager.ui.screens.dailyrecord.DailyRecordEntryScreen
import com.farm.layermanager.ui.screens.dashboard.DashboardScreen
import com.farm.layermanager.ui.screens.feed.FeedScreen
import com.farm.layermanager.ui.screens.finance.FinanceScreen
import com.farm.layermanager.ui.screens.health.HealthScreen
import com.farm.layermanager.ui.screens.house.HouseListScreen
import com.farm.layermanager.ui.screens.house.StrainListScreen
import com.farm.layermanager.ui.screens.inventory.InventoryScreen
import com.farm.layermanager.ui.screens.reports.ReportsScreen
import com.farm.layermanager.ui.screens.sales.SalesScreen

private data class BottomBarItem(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomBarItems = listOf(
    BottomBarItem(Screen.Dashboard, "الرئيسية", Icons.Default.Dashboard),
    BottomBarItem(Screen.HouseList, "العنابر", Icons.Default.Home),
    BottomBarItem(Screen.Feed, "العلف", Icons.Default.Grass),
    BottomBarItem(Screen.Inventory, "المخزون", Icons.Default.Inventory2),
    BottomBarItem(Screen.Sales, "المبيعات", Icons.Default.ShoppingCart),
    BottomBarItem(Screen.Finance, "المالية", Icons.Default.AttachMoney)
)

/**
 * الشاشات الخمس الرئيسية (Dashboard/العنابر/العلف/المبيعات/المالية) ظاهرة دائماً عبر شريط تنقل سفلي.
 * أما شاشات (السلالات، الإدخال اليومي، المخزون) فهي شاشات فرعية يُصَل إليها بالتنقل الخطي العادي
 * ولا تُظهر شريط التنقل السفلي فوقها (تجربة "التركيز الكامل" المناسبة لشاشات الإدخال).
 */
@Composable
fun LayerFarmNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = bottomBarItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(scaffoldPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(onOpenReports = { navController.navigate(Screen.Reports.route) })
            }
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Inventory.route) { InventoryScreen() }
            composable(Screen.Sales.route) { SalesScreen() }
            composable(Screen.Finance.route) { FinanceScreen() }

            composable(Screen.HouseList.route) {
                HouseListScreen(
                    onHouseClick = { houseId -> navController.navigate(Screen.StrainList.createRoute(houseId)) }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.StrainList.route,
                arguments = listOf(navArgument(Screen.ARG_HOUSE_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val houseId = backStackEntry.arguments?.getLong(Screen.ARG_HOUSE_ID) ?: return@composable
                StrainListScreen(
                    houseId = houseId,
                    onStrainClick = { strainId -> navController.navigate(Screen.DailyRecordEntry.createRoute(houseId, strainId)) },
                    onHealthClick = { strainId -> navController.navigate(Screen.Health.createRoute(houseId, strainId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Health.route,
                arguments = listOf(
                    navArgument(Screen.ARG_HOUSE_ID) { type = NavType.LongType },
                    navArgument(Screen.ARG_STRAIN_ID) { type = NavType.LongType }
                )
            ) {
                HealthScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.DailyRecordEntry.route,
                arguments = listOf(
                    navArgument(Screen.ARG_HOUSE_ID) { type = NavType.LongType },
                    navArgument(Screen.ARG_STRAIN_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val houseId = backStackEntry.arguments?.getLong(Screen.ARG_HOUSE_ID) ?: return@composable
                val strainId = backStackEntry.arguments?.getLong(Screen.ARG_STRAIN_ID) ?: return@composable
                DailyRecordEntryScreen(
                    houseId = houseId,
                    strainId = strainId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

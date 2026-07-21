package com.farm.layermanager.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.farm.layermanager.domain.usecase.reports.DashboardSnapshot
import com.farm.layermanager.ui.common.FullScreenLoading
import com.farm.layermanager.ui.common.StatCard
import com.farm.layermanager.ui.theme.BarnRed
import com.farm.layermanager.ui.theme.OliveDeep
import com.farm.layermanager.ui.theme.YolkAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenReports: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val snapshot by viewModel.snapshot.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة التحكم") },
                actions = {
                    IconButton(onClick = onOpenReports) {
                        Icon(Icons.Default.Assessment, contentDescription = "التقارير")
                    }
                }
            )
        }
    ) { padding ->
        val current = snapshot
        if (current == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) { FullScreenLoading() }
        } else {
            DashboardContent(snapshot = current, padding = padding)
        }
    }
}

@Composable
private fun DashboardContent(snapshot: DashboardSnapshot, padding: PaddingValues) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(padding)
    ) {
        item { SectionLabel("القطيع") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "العنابر النشطة", value = "${snapshot.activeHousesCount}", modifier = Modifier.weight(1f))
                StatCard(label = "الطيور الحالية", value = "${snapshot.currentBirdCount}", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "النافق اليوم", value = "${snapshot.mortalityToday}", accentColor = BarnRed, modifier = Modifier.weight(1f))
                StatCard(label = "نسبة النافق التراكمية", value = "%.2f%%".format(snapshot.mortalityPercentCumulative), accentColor = BarnRed, modifier = Modifier.weight(1f))
            }
        }

        item { SectionLabel("الإنتاج") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "إنتاج اليوم", value = "%.1f طبق".format(snapshot.productionToday), accentColor = YolkAmber, modifier = Modifier.weight(1f))
                StatCard(label = "Hen-Day اليوم", value = "%.1f%%".format(snapshot.henDayPercentToday), accentColor = YolkAmber, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "إنتاج الأسبوع", value = "%.1f طبق".format(snapshot.productionThisWeek), modifier = Modifier.weight(1f))
                StatCard(label = "إنتاج الشهر", value = "%.1f طبق".format(snapshot.productionThisMonth), modifier = Modifier.weight(1f))
            }
        }

        item { SectionLabel("المالية (الشهر الحالي)") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "مبيعات اليوم", value = "%.0f".format(snapshot.salesToday), modifier = Modifier.weight(1f))
                StatCard(label = "إجمالي الإيرادات", value = "%.0f".format(snapshot.totalRevenueThisMonth), modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "المصروفات", value = "%.0f".format(snapshot.totalExpensesThisMonth), accentColor = BarnRed, modifier = Modifier.weight(1f))
                StatCard(
                    label = "صافي الربح",
                    value = "%.0f".format(snapshot.netProfitThisMonth),
                    accentColor = if (snapshot.netProfitThisMonth >= 0) OliveDeep else BarnRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            StatCard(label = "إجمالي المديونية على العملاء", value = "%.0f".format(snapshot.outstandingDebt), accentColor = BarnRed, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

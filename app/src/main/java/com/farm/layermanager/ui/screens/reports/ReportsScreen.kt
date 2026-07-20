package com.farm.layermanager.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.farm.layermanager.domain.usecase.reports.PeriodReport
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.theme.BarnRed
import com.farm.layermanager.ui.theme.OliveDeep
import com.farm.layermanager.ui.theme.YolkAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val houses by viewModel.houses.collectAsStateWithLifecycle()
    val strains by viewModel.strainsForHouse(uiState.selectedHouseId ?: -1).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التقارير") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item { Text("نطاق التقرير", style = MaterialTheme.typography.titleMedium) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.scopeType == ReportScopeType.WHOLE_FARM,
                        onClick = { viewModel.onScopeTypeChange(ReportScopeType.WHOLE_FARM) },
                        label = { Text("كل المزرعة") }
                    )
                    FilterChip(
                        selected = uiState.scopeType == ReportScopeType.WHOLE_HOUSE,
                        onClick = { viewModel.onScopeTypeChange(ReportScopeType.WHOLE_HOUSE) },
                        label = { Text("عنبر كامل") }
                    )
                    FilterChip(
                        selected = uiState.scopeType == ReportScopeType.HOUSE_STRAIN,
                        onClick = { viewModel.onScopeTypeChange(ReportScopeType.HOUSE_STRAIN) },
                        label = { Text("عنبر × سلالة") }
                    )
                }
            }

            if (uiState.scopeType != ReportScopeType.WHOLE_FARM) {
                item { Text("اختر العنبر", style = MaterialTheme.typography.labelLarge) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        houses.forEach { house ->
                            FilterChip(
                                selected = uiState.selectedHouseId == house.houseId,
                                onClick = { viewModel.onHouseSelected(house.houseId) },
                                label = { Text(house.name) }
                            )
                        }
                    }
                }
            }

            if (uiState.scopeType == ReportScopeType.HOUSE_STRAIN && uiState.selectedHouseId != null) {
                item { Text("اختر السلالة", style = MaterialTheme.typography.labelLarge) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        strains.forEach { strain ->
                            FilterChip(
                                selected = uiState.selectedStrainId == strain.strainId,
                                onClick = { viewModel.onStrainSelected(strain.strainId) },
                                label = { Text(strain.strainName) }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "الفترة: ${uiState.startDate} إلى ${uiState.endDate} (الشهر الحالي افتراضياً)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Button(onClick = viewModel::onGenerate, modifier = Modifier.fillMaxWidth(), enabled = !uiState.isLoading) {
                    Text(if (uiState.isLoading) "جارٍ التوليد..." else "توليد التقرير")
                }
            }

            uiState.errorMessage?.let { message ->
                item { ErrorMessageCard(message = message) }
            }

            uiState.report?.let { report ->
                item { ReportResultCard(report) }
            }
        }
    }
}

@Composable
private fun ReportResultCard(report: PeriodReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("نتائج التقرير", style = MaterialTheme.typography.titleLarge)
            ReportLine("إجمالي النافق", "${report.totalMortality}", BarnRed)
            ReportLine("إجمالي المستبعد", "${report.totalCulled}", BarnRed)
            ReportLine("إجمالي الإنتاج", "%.1f طبق".format(report.totalProductionTrays), YolkAmber)
            ReportLine("متوسط Hen-Day", "%.1f%%".format(report.averageHenDayPercent), YolkAmber)
            ReportLine("إجمالي العلف المستهلك", "%.1f كجم".format(report.totalFeedConsumedKg), null)
            ReportLine("معامل التحويل الغذائي FCR", "%.2f".format(report.fcr), null)
            ReportLine("تكلفة العلف", "%.0f".format(report.totalFeedCost), BarnRed)
            ReportLine("تكلفة الصحة", "%.0f".format(report.totalHealthCost), BarnRed)
            ReportLine("إيرادات المبيعات", "%.0f".format(report.totalSalesRevenue), OliveDeep)
            ReportLine("إيرادات أخرى", "%.0f".format(report.totalOtherRevenue), OliveDeep)
            ReportLine("إجمالي المصروفات", "%.0f".format(report.totalExpenses), BarnRed)
            ReportLine("صافي الربح", "%.0f".format(report.netProfit), if (report.netProfit >= 0) OliveDeep else BarnRed)
        }
    }
}

@Composable
private fun ReportLine(label: String, value: String, accentColor: androidx.compose.ui.graphics.Color?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

package com.farm.layermanager.ui.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.farm.layermanager.domain.model.FeedType
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val feedTypes by viewModel.feedTypes.collectAsStateWithLifecycle()
    val typeForm by viewModel.typeFormState.collectAsStateWithLifecycle()
    val consumptionForm by viewModel.consumptionFormState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("العلف") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onOpenAddTypeDialog) {
                Icon(Icons.Default.Add, contentDescription = "إضافة نوع علف")
            }
        }
    ) { padding ->
        if (feedTypes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا توجد أنواع علف مسجَّلة بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(feedTypes, key = { it.feedTypeId }) { feedType ->
                    FeedTypeCard(
                        feedType = feedType,
                        onConsumeClick = { viewModel.onOpenConsumptionDialog(feedType.feedTypeId) }
                    )
                }
            }
        }

        if (typeForm.isDialogOpen) {
            AddFeedTypeDialog(state = typeForm, viewModel = viewModel)
        }
        if (consumptionForm.isDialogOpen) {
            RecordConsumptionDialog(state = consumptionForm, viewModel = viewModel)
        }
    }
}

@Composable
private fun FeedTypeCard(feedType: FeedType, onConsumeClick: () -> Unit) {
    val isLow = feedType.currentStockKg < feedType.bagWeightKg * 5
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLow) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = feedType.feedName, style = MaterialTheme.typography.titleLarge)
            feedType.company?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("الرصيد الحالي", style = MaterialTheme.typography.labelMedium)
                    Text("%.1f كجم".format(feedType.currentStockKg), style = MaterialTheme.typography.titleMedium)
                }
                Column {
                    Text("متوسط التكلفة", style = MaterialTheme.typography.labelMedium)
                    Text("%.2f".format(feedType.weightedAvgCost), style = MaterialTheme.typography.titleMedium)
                }
            }
            TextButton(onClick = onConsumeClick, modifier = Modifier.padding(top = 4.dp)) {
                Text("تسجيل استهلاك من هذا النوع")
            }
        }
    }
}

@Composable
private fun AddFeedTypeDialog(state: FeedTypeFormState, viewModel: FeedViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismissTypeDialog,
        title = { Text("إضافة نوع علف جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.feedName,
                    onValueChange = { v -> viewModel.onTypeFieldChange { copy(feedName = v) } },
                    label = { Text("اسم العلف") }, singleLine = true
                )
                OutlinedTextField(
                    value = state.company,
                    onValueChange = { v -> viewModel.onTypeFieldChange { copy(company = v) } },
                    label = { Text("الشركة (اختياري)") }, singleLine = true
                )
                NumericField(label = "السعر للكيلو", value = state.price, onValueChange = { v -> viewModel.onTypeFieldChange { copy(price = v) } })
                NumericField(label = "وزن الشيكارة (كجم)", value = state.bagWeightKg, onValueChange = { v -> viewModel.onTypeFieldChange { copy(bagWeightKg = v) } })
                NumericField(label = "الكمية الابتدائية (كجم)", value = state.quantityKg, onValueChange = { v -> viewModel.onTypeFieldChange { copy(quantityKg = v) } })
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveFeedType, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissTypeDialog) { Text("إلغاء") } }
    )
}

@Composable
private fun RecordConsumptionDialog(state: ConsumptionFormState, viewModel: FeedViewModel) {
    val houses by viewModel.houses.collectAsStateWithLifecycle()
    val strains by viewModel.strainsForHouse(state.selectedHouseId ?: -1).collectAsStateWithLifecycle(initialValue = emptyList())

    AlertDialog(
        onDismissRequest = viewModel::onDismissConsumptionDialog,
        title = { Text("تسجيل استهلاك علف") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("العنبر", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    houses.forEach { house ->
                        FilterChip(
                            selected = state.selectedHouseId == house.houseId,
                            onClick = { viewModel.onConsumptionHouseSelected(house.houseId) },
                            label = { Text(house.name) }
                        )
                    }
                }
                if (state.selectedHouseId != null) {
                    Text("السلالة", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        strains.forEach { strain ->
                            FilterChip(
                                selected = state.selectedStrainId == strain.strainId,
                                onClick = { viewModel.onConsumptionStrainSelected(strain.strainId) },
                                label = { Text(strain.strainName) }
                            )
                        }
                    }
                }
                NumericField(label = "عدد الشكاير", value = state.bagsCount, onValueChange = { v -> viewModel.onConsumptionFieldChange { copy(bagsCount = v) } })
                NumericField(label = "الوزن الإجمالي (كجم)", value = state.totalWeightKg, onValueChange = { v -> viewModel.onConsumptionFieldChange { copy(totalWeightKg = v) } })
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveConsumption, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissConsumptionDialog) { Text("إلغاء") } }
    )
}

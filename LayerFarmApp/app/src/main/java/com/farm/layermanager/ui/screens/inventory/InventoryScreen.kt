package com.farm.layermanager.ui.screens.inventory

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.farm.layermanager.domain.model.InventoryItem
import com.farm.layermanager.domain.model.InventoryTransactionType
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val itemForm by viewModel.itemForm.collectAsStateWithLifecycle()
    val transactionForm by viewModel.transactionForm.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = if (searchQuery.isBlank()) items else items.filter {
        it.itemName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("المخزون العام") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onOpenAddItemDialog) {
                Icon(Icons.Default.Add, contentDescription = "إضافة صنف")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا توجد أصناف مسجَّلة بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("بحث عن صنف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(filteredItems, key = { it.itemId }) { item ->
                    InventoryItemCard(
                        item = item,
                        onInClick = { viewModel.onOpenTransactionDialog(item.itemId, InventoryTransactionType.IN) },
                        onOutClick = { viewModel.onOpenTransactionDialog(item.itemId, InventoryTransactionType.OUT) }
                    )
                }
            }
        }

        if (itemForm.isDialogOpen) AddInventoryItemDialog(state = itemForm, viewModel = viewModel)
        if (transactionForm.isDialogOpen) InventoryTransactionDialog(state = transactionForm, viewModel = viewModel)
    }
}

@Composable
private fun InventoryItemCard(item: InventoryItem, onInClick: () -> Unit, onOutClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBelowThreshold) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.itemName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${item.category.name} • الرصيد: ${item.currentStock} ${item.unit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.isBelowThreshold) {
                Text(
                    text = "⚠ الرصيد عند أو تحت الحد الأدنى (${item.minThreshold} ${item.unit})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                TextButton(onClick = onInClick) { Text("تسجيل وارد") }
                TextButton(onClick = onOutClick) { Text("تسجيل صادر") }
            }
        }
    }
}

@Composable
private fun AddInventoryItemDialog(state: InventoryItemFormState, viewModel: InventoryViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismissAddItemDialog,
        title = { Text("إضافة صنف جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.itemName,
                    onValueChange = { v -> viewModel.onItemFieldChange { copy(itemName = v) } },
                    label = { Text("اسم الصنف") }, singleLine = true
                )
                OutlinedTextField(
                    value = state.unit,
                    onValueChange = { v -> viewModel.onItemFieldChange { copy(unit = v) } },
                    label = { Text("الوحدة (علبة، لتر...)") }, singleLine = true
                )
                NumericField(
                    label = "الحد الأدنى للتنبيه", value = state.minThreshold,
                    onValueChange = { v -> viewModel.onItemFieldChange { copy(minThreshold = v) } }
                )
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveItem, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissAddItemDialog) { Text("إلغاء") } }
    )
}

@Composable
private fun InventoryTransactionDialog(state: TransactionFormState, viewModel: InventoryViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismissTransactionDialog,
        title = { Text(if (state.type == InventoryTransactionType.IN) "تسجيل وارد" else "تسجيل صادر") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.type == InventoryTransactionType.IN,
                        onClick = { viewModel.onTransactionFieldChange { copy(type = InventoryTransactionType.IN) } },
                        label = { Text("وارد") }
                    )
                    FilterChip(
                        selected = state.type == InventoryTransactionType.OUT,
                        onClick = { viewModel.onTransactionFieldChange { copy(type = InventoryTransactionType.OUT) } },
                        label = { Text("صادر") }
                    )
                }
                NumericField(label = "الكمية", value = state.quantity, onValueChange = { v -> viewModel.onTransactionFieldChange { copy(quantity = v) } })
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { v -> viewModel.onTransactionFieldChange { copy(notes = v) } },
                    label = { Text("ملاحظات (اختياري)") }
                )
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveTransaction, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissTransactionDialog) { Text("إلغاء") } }
    )
}

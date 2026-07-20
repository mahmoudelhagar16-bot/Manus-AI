package com.farm.layermanager.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.farm.layermanager.domain.model.Expense
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField
import com.farm.layermanager.ui.common.StatCard
import com.farm.layermanager.ui.theme.BarnRed
import com.farm.layermanager.ui.theme.OliveDeep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val expenses by viewModel.expensesThisMonth.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpensesThisMonth.collectAsStateWithLifecycle()
    val netProfit by viewModel.netProfitThisMonth.collectAsStateWithLifecycle()
    val expenseForm by viewModel.expenseForm.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("المالية") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onOpenExpenseDialog) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مصروف")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(padding)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = "صافي الربح هذا الشهر",
                        value = "%.0f".format(netProfit),
                        accentColor = if (netProfit >= 0) OliveDeep else BarnRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "إجمالي المصروفات",
                        value = "%.0f".format(totalExpenses),
                        accentColor = BarnRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item { Text("مصروفات هذا الشهر", style = MaterialTheme.typography.titleMedium) }
            items(expenses, key = { it.expenseId }) { expense ->
                ExpenseRow(expense = expense, categoryName = categories.firstOrNull { it.categoryId == expense.categoryId }?.categoryName ?: "")
            }
        }

        if (expenseForm.isDialogOpen) {
            AddExpenseDialog(
                state = expenseForm,
                categories = categories,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, categoryName: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = categoryName, style = MaterialTheme.typography.titleMedium)
                expense.description?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(text = "%.0f".format(expense.amount), style = MaterialTheme.typography.titleMedium, color = BarnRed)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    state: ExpenseFormState,
    categories: List<com.farm.layermanager.domain.model.ExpenseCategory>,
    viewModel: FinanceViewModel
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedCategoryName = categories.firstOrNull { it.categoryId == state.categoryId }?.categoryName ?: "اختر فئة"

    AlertDialog(
        onDismissRequest = viewModel::onDismissExpenseDialog,
        title = { Text("إضافة مصروف") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { dropdownExpanded = true }) { Text(selectedCategoryName) }
                DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.categoryName) },
                            onClick = {
                                viewModel.onExpenseFieldChange { copy(categoryId = category.categoryId) }
                                dropdownExpanded = false
                            }
                        )
                    }
                }
                NumericField(label = "المبلغ", value = state.amount, onValueChange = { v -> viewModel.onExpenseFieldChange { copy(amount = v) } })
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { v -> viewModel.onExpenseFieldChange { copy(description = v) } },
                    label = { Text("وصف (اختياري)") }
                )
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveExpense, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissExpenseDialog) { Text("إلغاء") } }
    )
}

package com.farm.layermanager.ui.screens.sales

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.farm.layermanager.domain.model.Customer
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField
import com.farm.layermanager.ui.common.StatCard
import com.farm.layermanager.ui.theme.BarnRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: SalesViewModel = hiltViewModel()) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val eggBalance by viewModel.eggBalance.collectAsStateWithLifecycle()
    val outstandingDebt by viewModel.outstandingDebt.collectAsStateWithLifecycle()
    val saleForm by viewModel.saleForm.collectAsStateWithLifecycle()
    val customerForm by viewModel.customerForm.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val filteredCustomers = if (searchQuery.isBlank()) customers else customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || (it.phone?.contains(searchQuery) == true)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("المبيعات") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::onOpenCustomerDialog,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("عميل جديد") }
            )
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
                        label = "رصيد البيض المتاح للبيع",
                        value = "%.1f طبق".format(eggBalance?.availableTrays ?: 0.0),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "إجمالي المديونية",
                        value = "%.0f".format(outstandingDebt),
                        accentColor = BarnRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Text("العملاء", style = MaterialTheme.typography.titleMedium)
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("بحث عن عميل") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            items(filteredCustomers, key = { it.customerId }) { customer ->
                CustomerCard(customer = customer, onSellClick = { viewModel.onOpenSaleDialog(customer.customerId) })
            }
        }

        if (saleForm.isDialogOpen) {
            RecordSaleDialog(state = saleForm, availableTrays = eggBalance?.availableTrays ?: 0.0, viewModel = viewModel)
        }
        if (customerForm.isDialogOpen) {
            AddCustomerDialog(state = customerForm, viewModel = viewModel)
        }
    }
}

@Composable
private fun CustomerCard(customer: Customer, onSellClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = customer.name, style = MaterialTheme.typography.titleMedium)
                customer.phone?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            TextButton(onClick = onSellClick) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("تسجيل بيع")
            }
        }
    }
}

@Composable
private fun RecordSaleDialog(state: SaleFormState, availableTrays: Double, viewModel: SalesViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismissSaleDialog,
        title = { Text("تسجيل بيع (المتاح: %.1f طبق)".format(availableTrays)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericField(label = "أبيض (طبق)", value = state.whiteTrays, onValueChange = { v -> viewModel.onSaleFieldChange { copy(whiteTrays = v) } }, modifier = Modifier.weight(1f))
                    NumericField(label = "أحمر (طبق)", value = state.redTrays, onValueChange = { v -> viewModel.onSaleFieldChange { copy(redTrays = v) } }, modifier = Modifier.weight(1f))
                    NumericField(label = "مكسور (طبق)", value = state.crackedTrays, onValueChange = { v -> viewModel.onSaleFieldChange { copy(crackedTrays = v) } }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericField(label = "سعر الأبيض", value = state.whitePrice, onValueChange = { v -> viewModel.onSaleFieldChange { copy(whitePrice = v) } }, modifier = Modifier.weight(1f))
                    NumericField(label = "سعر الأحمر", value = state.redPrice, onValueChange = { v -> viewModel.onSaleFieldChange { copy(redPrice = v) } }, modifier = Modifier.weight(1f))
                    NumericField(label = "سعر المكسور", value = state.crackedPrice, onValueChange = { v -> viewModel.onSaleFieldChange { copy(crackedPrice = v) } }, modifier = Modifier.weight(1f))
                }
                NumericField(label = "المبلغ المدفوع الآن", value = state.paidAmount, onValueChange = { v -> viewModel.onSaleFieldChange { copy(paidAmount = v) } })
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveSale, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissSaleDialog) { Text("إلغاء") } }
    )
}

@Composable
private fun AddCustomerDialog(state: CustomerFormState, viewModel: SalesViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismissCustomerDialog,
        title = { Text("عميل جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.name, onValueChange = { v -> viewModel.onCustomerFieldChange { copy(name = v) } }, label = { Text("الاسم") }, singleLine = true)
                OutlinedTextField(value = state.phone, onValueChange = { v -> viewModel.onCustomerFieldChange { copy(phone = v) } }, label = { Text("الهاتف (اختياري)") }, singleLine = true)
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveCustomer, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissCustomerDialog) { Text("إلغاء") } }
    )
}

package com.farm.layermanager.ui.screens.house

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.HouseStatus
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseListScreen(
    onHouseClick: (Long) -> Unit,
    viewModel: HouseListViewModel = hiltViewModel()
) {
    val houses by viewModel.houses.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("العنابر") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onOpenAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عنبر")
            }
        }
    ) { padding ->
        if (houses.isEmpty()) {
            EmptyHousesMessage(padding)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(houses, key = { it.houseId }) { house ->
                    HouseCard(
                        house = house,
                        onClick = { onHouseClick(house.houseId) },
                        onToggleActive = { viewModel.onToggleActive(house) }
                    )
                }
            }
        }

        if (uiState.isAddDialogOpen) {
            AddHouseDialog(
                name = uiState.newHouseName,
                number = uiState.newHouseNumber,
                notes = uiState.newHouseNotes,
                errorMessage = uiState.errorMessage,
                isSaving = uiState.isSaving,
                onNameChange = viewModel::onNameChange,
                onNumberChange = viewModel::onNumberChange,
                onNotesChange = viewModel::onNotesChange,
                onDismiss = viewModel::onDismissAddDialog,
                onConfirm = viewModel::onSaveHouse
            )
        }
    }
}

@Composable
private fun EmptyHousesMessage(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "لا توجد عنابر بعد. اضغط + لإضافة أول عنبر",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HouseCard(house: House, onClick: () -> Unit, onToggleActive: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (house.status == HouseStatus.ACTIVE)
                MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = house.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "رقم العنبر: ${house.number}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            house.notes?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onToggleActive) {
                Text(if (house.status == HouseStatus.ACTIVE) "تعطيل العنبر" else "إعادة تفعيل العنبر")
            }
        }
    }
}

@Composable
private fun AddHouseDialog(
    name: String,
    number: String,
    notes: String,
    errorMessage: String?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة عنبر جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("اسم العنبر") }, singleLine = true)
                NumericField(label = "رقم العنبر", value = number, onValueChange = onNumberChange, allowDecimal = false)
                OutlinedTextField(value = notes, onValueChange = onNotesChange, label = { Text("ملاحظات (اختياري)") })
                errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

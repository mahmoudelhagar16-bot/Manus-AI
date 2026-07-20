package com.farm.layermanager.ui.screens.health

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.farm.layermanager.domain.model.Medication
import com.farm.layermanager.domain.model.Vaccination
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField
import com.farm.layermanager.ui.theme.BarnRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    onBack: () -> Unit,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val vaccinations by viewModel.vaccinations.collectAsStateWithLifecycle()
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val vaccinationForm by viewModel.vaccinationForm.collectAsStateWithLifecycle()
    val medicationForm by viewModel.medicationForm.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الصحة") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (selectedTab == 0) viewModel.onOpenVaccinationDialog() else viewModel.onOpenMedicationDialog() }
            ) { Icon(Icons.Default.Add, contentDescription = "إضافة") }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("التحصينات") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("الأدوية") })
            }

            if (selectedTab == 0) {
                VaccinationsList(vaccinations)
            } else {
                MedicationsList(medications)
            }
        }

        if (vaccinationForm.isDialogOpen) AddVaccinationDialog(state = vaccinationForm, viewModel = viewModel)
        if (medicationForm.isDialogOpen) AddMedicationDialog(state = medicationForm, viewModel = viewModel)
    }
}

@Composable
private fun VaccinationsList(vaccinations: List<Vaccination>) {
    if (vaccinations.isEmpty()) {
        EmptyState("لا توجد تحصينات مسجَّلة بعد")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(vaccinations, key = { it.vaccinationId }) { v ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = v.vaccineName, style = MaterialTheme.typography.titleMedium)
                    Text(text = v.vDate.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    v.dose?.let { Text(text = "الجرعة: $it", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}

@Composable
private fun MedicationsList(medications: List<Medication>) {
    if (medications.isEmpty()) {
        EmptyState("لا توجد أدوية مسجَّلة بعد")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(medications, key = { it.medicationId }) { m ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = m.medicineName, style = MaterialTheme.typography.titleMedium)
                    Text(text = m.mDate.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    m.reason?.let { Text(text = "السبب: $it", style = MaterialTheme.typography.bodyMedium) }
                    Text(text = "التكلفة: %.2f".format(m.cost), style = MaterialTheme.typography.bodyMedium, color = BarnRed)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddVaccinationDialog(state: VaccinationFormState, viewModel: HealthViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismissVaccinationDialog,
        title = { Text("إضافة تحصين") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.vaccineName, onValueChange = { v -> viewModel.onVaccinationFieldChange { copy(vaccineName = v) } }, label = { Text("اسم التحصين") }, singleLine = true)
                OutlinedTextField(value = state.company, onValueChange = { v -> viewModel.onVaccinationFieldChange { copy(company = v) } }, label = { Text("الشركة (اختياري)") }, singleLine = true)
                OutlinedTextField(value = state.dose, onValueChange = { v -> viewModel.onVaccinationFieldChange { copy(dose = v) } }, label = { Text("الجرعة (اختياري)") }, singleLine = true)
                OutlinedTextField(value = state.method, onValueChange = { v -> viewModel.onVaccinationFieldChange { copy(method = v) } }, label = { Text("طريقة الإعطاء (اختياري)") }, singleLine = true)
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveVaccination, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissVaccinationDialog) { Text("إلغاء") } }
    )
}

@Composable
private fun AddMedicationDialog(state: MedicationFormState, viewModel: HealthViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onDismissMedicationDialog,
        title = { Text("إضافة دواء") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.medicineName, onValueChange = { v -> viewModel.onMedicationFieldChange { copy(medicineName = v) } }, label = { Text("اسم الدواء") }, singleLine = true)
                OutlinedTextField(value = state.reason, onValueChange = { v -> viewModel.onMedicationFieldChange { copy(reason = v) } }, label = { Text("السبب (اختياري)") }, singleLine = true)
                OutlinedTextField(value = state.dose, onValueChange = { v -> viewModel.onMedicationFieldChange { copy(dose = v) } }, label = { Text("الجرعة (اختياري)") }, singleLine = true)
                NumericField(label = "مدة العلاج (أيام)", value = state.durationDays, allowDecimal = false, onValueChange = { v -> viewModel.onMedicationFieldChange { copy(durationDays = v) } })
                NumericField(label = "التكلفة", value = state.cost, onValueChange = { v -> viewModel.onMedicationFieldChange { copy(cost = v) } })
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::onSaveMedication, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = viewModel::onDismissMedicationDialog) { Text("إلغاء") } }
    )
}

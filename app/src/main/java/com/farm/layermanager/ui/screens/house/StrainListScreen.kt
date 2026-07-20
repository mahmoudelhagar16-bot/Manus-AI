package com.farm.layermanager.ui.screens.house

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.farm.layermanager.domain.model.EggColor
import com.farm.layermanager.domain.model.StrainStats
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrainListScreen(
    houseId: Long,
    onStrainClick: (Long) -> Unit,
    onHealthClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: StrainListViewModel = hiltViewModel()
) {
    val stats by viewModel.strainStats.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("السلالات") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onOpenAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "إضافة سلالة")
            }
        }
    ) { padding ->
        if (stats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا توجد سلالات في هذا العنبر بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(stats, key = { it.strain.strainId }) { stat ->
                    StrainStatsCard(
                        stat = stat,
                        onClick = { onStrainClick(stat.strain.strainId) },
                        onHealthClick = { onHealthClick(stat.strain.strainId) }
                    )
                }
            }
        }

        if (formState.isDialogOpen) {
            AddStrainDialog(
                state = formState,
                onNameChange = viewModel::onNameChange,
                onEggColorChange = viewModel::onEggColorChange,
                onCountChange = viewModel::onInitialCountChange,
                onDismiss = viewModel::onDismissDialog,
                onConfirm = viewModel::onSave
            )
        }
    }
}

@Composable
private fun StrainStatsCard(stat: StrainStats, onClick: () -> Unit, onHealthClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stat.strain.strainName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "العمر: ${stat.ageInWeeks} أسبوع  •  ${if (stat.strain.eggColor == EggColor.WHITE) "بيض أبيض" else "بيض أحمر"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStat(label = "الطيور الحالية", value = "${stat.currentBirdCount}")
                MiniStat(label = "نسبة البقاء", value = "%.1f%%".format(stat.livabilityPercent))
                MiniStat(label = "النافق التراكمي", value = "${stat.cumulativeMortality}")
            }
            TextButton(onClick = onHealthClick, modifier = Modifier.padding(top = 4.dp)) {
                Text("سجل الصحة (تحصينات/أدوية)")
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddStrainDialog(
    state: AddStrainFormState,
    onNameChange: (String) -> Unit,
    onEggColorChange: (EggColor) -> Unit,
    onCountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة سلالة جديدة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.strainName, onValueChange = onNameChange, label = { Text("اسم السلالة") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.eggColor == EggColor.WHITE,
                        onClick = { onEggColorChange(EggColor.WHITE) },
                        label = { Text("بيض أبيض") }
                    )
                    FilterChip(
                        selected = state.eggColor == EggColor.RED,
                        onClick = { onEggColorChange(EggColor.RED) },
                        label = { Text("بيض أحمر") }
                    )
                }
                NumericField(label = "عدد الكتاكيت الابتدائي", value = state.initialChickCount, onValueChange = onCountChange, allowDecimal = false)
                state.errorMessage?.let { ErrorMessageCard(message = it) }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm, enabled = !state.isSaving) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

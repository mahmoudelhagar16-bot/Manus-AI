package com.farm.layermanager.ui.screens.dailyrecord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.farm.layermanager.ui.common.ErrorMessageCard
import com.farm.layermanager.ui.common.NumericField
import com.farm.layermanager.ui.common.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRecordEntryScreen(
    houseId: Long,
    strainId: Long,
    onBack: () -> Unit,
    viewModel: DailyRecordViewModel = hiltViewModel()
) {
    val strain by viewModel.strain.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val baseline = viewModel.currentBaseline()

    LaunchedEffect(formState.savedSuccessfully) {
        if (formState.savedSuccessfully) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strain?.strainName ?: "السجل اليومي") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item {
                StatCard(
                    label = "عدد الطيور الحية المرجعي (قبل اليوم)",
                    value = "$baseline طائر",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SectionTitle("القطيع")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericField(
                        label = "نافق اليوم", value = formState.mortality, allowDecimal = false,
                        onValueChange = { v -> viewModel.onFieldChange { copy(mortality = v) } },
                        modifier = Modifier.weight(1f)
                    )
                    NumericField(
                        label = "مستبعد اليوم", value = formState.culled, allowDecimal = false,
                        onValueChange = { v -> viewModel.onFieldChange { copy(culled = v) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { SectionTitle("العلف والمياه") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericField(
                        label = "العلف (كجم)", value = formState.feedQtyKg,
                        onValueChange = { v -> viewModel.onFieldChange { copy(feedQtyKg = v) } },
                        modifier = Modifier.weight(1f)
                    )
                    NumericField(
                        label = "المياه (لتر)", value = formState.waterLiters,
                        onValueChange = { v -> viewModel.onFieldChange { copy(waterLiters = v) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { SectionTitle("البيئة (اختياري)") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericField(
                        label = "الحرارة °م", value = formState.temperature,
                        onValueChange = { v -> viewModel.onFieldChange { copy(temperature = v) } },
                        modifier = Modifier.weight(1f)
                    )
                    NumericField(
                        label = "الرطوبة %", value = formState.humidity,
                        onValueChange = { v -> viewModel.onFieldChange { copy(humidity = v) } },
                        modifier = Modifier.weight(1f)
                    )
                    NumericField(
                        label = "الإضاءة (ساعة)", value = formState.lightHours,
                        onValueChange = { v -> viewModel.onFieldChange { copy(lightHours = v) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { SectionTitle("الإنتاج") }
            item {
                NumericField(
                    label = "عدد الأطباق المنتجة",
                    value = formState.productionTrays,
                    onValueChange = { v -> viewModel.onFieldChange { copy(productionTrays = v) } }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericField(
                        label = "بيض مكسور", value = formState.crackedEggs, allowDecimal = false,
                        onValueChange = { v -> viewModel.onFieldChange { copy(crackedEggs = v) } },
                        modifier = Modifier.weight(1f)
                    )
                    NumericField(
                        label = "بيض مشوّه", value = formState.deformedEggs, allowDecimal = false,
                        onValueChange = { v -> viewModel.onFieldChange { copy(deformedEggs = v) } },
                        modifier = Modifier.weight(1f)
                    )
                    NumericField(
                        label = "بيض أرضي", value = formState.floorEggs, allowDecimal = false,
                        onValueChange = { v -> viewModel.onFieldChange { copy(floorEggs = v) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = { v -> viewModel.onFieldChange { copy(notes = v) } },
                    label = { Text("ملاحظات (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            formState.errorMessage?.let { message ->
                item { ErrorMessageCard(message = message) }
            }

            item {
                Button(
                    onClick = viewModel::onSave,
                    enabled = !formState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (formState.isSaving) "جارٍ الحفظ..." else "حفظ السجل اليومي")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

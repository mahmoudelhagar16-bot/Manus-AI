package com.farm.layermanager.ui.screens.dailyrecord

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.DailyRecord
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.repository.StrainRepository
import com.farm.layermanager.domain.usecase.dailyrecord.AddDailyRecordUseCase
import com.farm.layermanager.domain.usecase.dailyrecord.GetDailyRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DailyRecordFormState(
    val recordDate: LocalDate = LocalDate.now(),
    val mortality: String = "0",
    val culled: String = "0",
    val feedQtyKg: String = "",
    val waterLiters: String = "",
    val temperature: String = "",
    val humidity: String = "",
    val lightHours: String = "",
    val productionTrays: String = "",
    val crackedEggs: String = "0",
    val deformedEggs: String = "0",
    val floorEggs: String = "0",
    val notes: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

/**
 * يحسب "baseline" الطيور الحية تلقائياً (آخر liveBirds سابق، أو initialChickCount لأول سجل)،
 * ويعرضه في الواجهة قبل الإدخال حتى يعرف المزارع الرقم المرجعي دون حسابه يدوياً — تجربة ميدانية أسرع.
 */
@HiltViewModel
class DailyRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addDailyRecordUseCase: AddDailyRecordUseCase,
    private val getDailyRecordsUseCase: GetDailyRecordsUseCase,
    private val strainRepository: StrainRepository
) : ViewModel() {

    val houseId: Long = checkNotNull(savedStateHandle["houseId"])
    val strainId: Long = checkNotNull(savedStateHandle["strainId"])

    val strain: StateFlow<Strain?> = strainRepository.getById(strainId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentRecords: StateFlow<List<DailyRecord>> = getDailyRecordsUseCase.getByStrain(houseId, strainId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(DailyRecordFormState())
    val formState: StateFlow<DailyRecordFormState> = _formState

    /** baseline = liveBirds في أحدث سجل موجود بالفعل بين recentRecords، أو initialChickCount إن لم يوجد أي سجل بعد. */
    fun currentBaseline(): Int {
        val latest = recentRecords.value.maxByOrNull { it.recordDate }
        return latest?.liveBirds ?: strain.value?.initialChickCount ?: 0
    }

    fun onFieldChange(update: DailyRecordFormState.() -> DailyRecordFormState) {
        _formState.value = _formState.value.update().copy(errorMessage = null)
    }

    fun onSave() {
        val state = _formState.value
        val baseline = currentBaseline()
        val mortality = state.mortality.toIntOrNull()
        val culled = state.culled.toIntOrNull()
        val feedQtyKg = state.feedQtyKg.toDoubleOrNull()
        val waterLiters = state.waterLiters.toDoubleOrNull()
        val productionTrays = state.productionTrays.toDoubleOrNull()

        if (mortality == null || culled == null || feedQtyKg == null || waterLiters == null || productionTrays == null) {
            _formState.value = state.copy(errorMessage = "الرجاء تعبئة كل الحقول الرقمية الأساسية (نافق، مستبعد، علف، مياه، إنتاج)")
            return
        }

        val liveBirds = baseline - mortality - culled

        val record = DailyRecord(
            recordDate = state.recordDate,
            houseId = houseId,
            strainId = strainId,
            liveBirds = liveBirds,
            mortality = mortality,
            culled = culled,
            feedQtyKg = feedQtyKg,
            waterLiters = waterLiters,
            temperature = state.temperature.toDoubleOrNull(),
            humidity = state.humidity.toDoubleOrNull(),
            lightHours = state.lightHours.toDoubleOrNull(),
            productionTrays = productionTrays,
            crackedEggs = state.crackedEggs.toIntOrNull() ?: 0,
            deformedEggs = state.deformedEggs.toIntOrNull() ?: 0,
            floorEggs = state.floorEggs.toIntOrNull() ?: 0,
            notes = state.notes.ifBlank { null }
        )

        viewModelScope.launch {
            _formState.value = state.copy(isSaving = true, errorMessage = null)
            addDailyRecordUseCase(record)
                .onSuccess { _formState.value = DailyRecordFormState(savedSuccessfully = true) }
                .onError { message -> _formState.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }
}

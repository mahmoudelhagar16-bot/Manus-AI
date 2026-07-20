package com.farm.layermanager.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.usecase.house.GetHousesUseCase
import com.farm.layermanager.domain.usecase.reports.GenerateReportUseCase
import com.farm.layermanager.domain.usecase.reports.PeriodReport
import com.farm.layermanager.domain.usecase.reports.ReportScope
import com.farm.layermanager.domain.usecase.strain.GetStrainDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class ReportScopeType { WHOLE_FARM, WHOLE_HOUSE, HOUSE_STRAIN }

data class ReportsUiState(
    val scopeType: ReportScopeType = ReportScopeType.WHOLE_FARM,
    val selectedHouseId: Long? = null,
    val selectedStrainId: Long? = null,
    val startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    val endDate: LocalDate = LocalDate.now(),
    val report: PeriodReport? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val generateReportUseCase: GenerateReportUseCase,
    private val getHousesUseCase: GetHousesUseCase,
    private val getStrainDetailsUseCase: GetStrainDetailsUseCase
) : ViewModel() {

    val houses: StateFlow<List<House>> = getHousesUseCase.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    /** يُستدعى عند اختيار عنبر — يجلب سلالاته تحديداً. يُعاد Flow خام (وليس StateFlow) ليتجنب إنشاء مُجمِّع جديد دائم في viewModelScope عند كل استدعاء. */
    fun strainsForHouse(houseId: Long): kotlinx.coroutines.flow.Flow<List<Strain>> =
        getStrainDetailsUseCase.getByHouse(houseId)

    fun onScopeTypeChange(type: ReportScopeType) {
        _uiState.value = _uiState.value.copy(scopeType = type, selectedHouseId = null, selectedStrainId = null, report = null)
    }

    fun onHouseSelected(houseId: Long) {
        _uiState.value = _uiState.value.copy(selectedHouseId = houseId, selectedStrainId = null, report = null)
    }

    fun onStrainSelected(strainId: Long) {
        _uiState.value = _uiState.value.copy(selectedStrainId = strainId, report = null)
    }

    fun onStartDateChange(date: LocalDate) { _uiState.value = _uiState.value.copy(startDate = date, report = null) }
    fun onEndDateChange(date: LocalDate) { _uiState.value = _uiState.value.copy(endDate = date, report = null) }

    fun onGenerate() {
        val state = _uiState.value
        val scope = when (state.scopeType) {
            ReportScopeType.WHOLE_FARM -> ReportScope.WholeFarm
            ReportScopeType.WHOLE_HOUSE -> {
                val houseId = state.selectedHouseId
                if (houseId == null) {
                    _uiState.value = state.copy(errorMessage = "الرجاء اختيار عنبر")
                    return
                }
                ReportScope.WholeHouse(houseId)
            }
            ReportScopeType.HOUSE_STRAIN -> {
                val houseId = state.selectedHouseId
                val strainId = state.selectedStrainId
                if (houseId == null || strainId == null) {
                    _uiState.value = state.copy(errorMessage = "الرجاء اختيار عنبر وسلالة")
                    return
                }
                ReportScope.HouseStrain(houseId, strainId)
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            runCatching {
                generateReportUseCase(scope, state.startDate, state.endDate)
            }.onSuccess { report ->
                _uiState.value = _uiState.value.copy(isLoading = false, report = report)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = throwable.message ?: "تعذَّر توليد التقرير")
            }
        }
    }
}

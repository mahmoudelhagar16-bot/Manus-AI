package com.farm.layermanager.ui.screens.health

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.Medication
import com.farm.layermanager.domain.model.Vaccination
import com.farm.layermanager.domain.usecase.health.AddMedicationUseCase
import com.farm.layermanager.domain.usecase.health.AddVaccinationUseCase
import com.farm.layermanager.domain.usecase.health.GetMedicationsUseCase
import com.farm.layermanager.domain.usecase.health.GetVaccinationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class VaccinationFormState(
    val isDialogOpen: Boolean = false,
    val vaccineName: String = "",
    val company: String = "",
    val dose: String = "",
    val method: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

data class MedicationFormState(
    val isDialogOpen: Boolean = false,
    val medicineName: String = "",
    val reason: String = "",
    val dose: String = "",
    val durationDays: String = "",
    val cost: String = "0",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVaccinationsUseCase: GetVaccinationsUseCase,
    private val addVaccinationUseCase: AddVaccinationUseCase,
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val addMedicationUseCase: AddMedicationUseCase
) : ViewModel() {

    val houseId: Long = checkNotNull(savedStateHandle["houseId"])
    val strainId: Long = checkNotNull(savedStateHandle["strainId"])

    val vaccinations: StateFlow<List<Vaccination>> = getVaccinationsUseCase.getByStrain(strainId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<Medication>> = getMedicationsUseCase.getByStrain(strainId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _vaccinationForm = MutableStateFlow(VaccinationFormState())
    val vaccinationForm: StateFlow<VaccinationFormState> = _vaccinationForm

    private val _medicationForm = MutableStateFlow(MedicationFormState())
    val medicationForm: StateFlow<MedicationFormState> = _medicationForm

    fun onOpenVaccinationDialog() { _vaccinationForm.value = VaccinationFormState(isDialogOpen = true) }
    fun onDismissVaccinationDialog() { _vaccinationForm.value = VaccinationFormState() }
    fun onVaccinationFieldChange(update: VaccinationFormState.() -> VaccinationFormState) {
        _vaccinationForm.value = _vaccinationForm.value.update().copy(errorMessage = null)
    }

    fun onSaveVaccination() {
        val state = _vaccinationForm.value
        viewModelScope.launch {
            _vaccinationForm.value = state.copy(isSaving = true, errorMessage = null)
            val vaccination = Vaccination(
                vDate = LocalDate.now(),
                houseId = houseId,
                strainId = strainId,
                vaccineName = state.vaccineName,
                company = state.company.ifBlank { null },
                dose = state.dose.ifBlank { null },
                method = state.method.ifBlank { null }
            )
            addVaccinationUseCase(vaccination)
                .onSuccess { _vaccinationForm.value = VaccinationFormState() }
                .onError { message -> _vaccinationForm.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }

    fun onOpenMedicationDialog() { _medicationForm.value = MedicationFormState(isDialogOpen = true) }
    fun onDismissMedicationDialog() { _medicationForm.value = MedicationFormState() }
    fun onMedicationFieldChange(update: MedicationFormState.() -> MedicationFormState) {
        _medicationForm.value = _medicationForm.value.update().copy(errorMessage = null)
    }

    fun onSaveMedication() {
        val state = _medicationForm.value
        viewModelScope.launch {
            _medicationForm.value = state.copy(isSaving = true, errorMessage = null)
            val medication = Medication(
                mDate = LocalDate.now(),
                houseId = houseId,
                strainId = strainId,
                medicineName = state.medicineName,
                reason = state.reason.ifBlank { null },
                dose = state.dose.ifBlank { null },
                durationDays = state.durationDays.toIntOrNull(),
                cost = state.cost.toDoubleOrNull() ?: 0.0
            )
            addMedicationUseCase(medication)
                .onSuccess { _medicationForm.value = MedicationFormState() }
                .onError { message -> _medicationForm.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }
}

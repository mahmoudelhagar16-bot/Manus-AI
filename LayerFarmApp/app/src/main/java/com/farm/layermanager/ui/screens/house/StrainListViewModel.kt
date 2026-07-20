package com.farm.layermanager.ui.screens.house

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.EggColor
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.model.StrainStats
import com.farm.layermanager.domain.usecase.strain.AddStrainUseCase
import com.farm.layermanager.domain.usecase.strain.GetStrainDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddStrainFormState(
    val isDialogOpen: Boolean = false,
    val strainName: String = "",
    val eggColor: EggColor = EggColor.WHITE,
    val initialChickCount: String = "",
    val arrivalDate: LocalDate = LocalDate.now(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class StrainListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStrainDetailsUseCase: GetStrainDetailsUseCase,
    private val addStrainUseCase: AddStrainUseCase
) : ViewModel() {

    val houseId: Long = checkNotNull(savedStateHandle["houseId"])

    val strainStats: StateFlow<List<StrainStats>> = getStrainDetailsUseCase.getByHouse(houseId)
        .combine(getStrainDetailsUseCase.getAllStats()) { strains, allStats ->
            val idsInHouse = strains.map { it.strainId }.toSet()
            allStats.filter { it.strain.strainId in idsInHouse }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(AddStrainFormState())
    val formState: StateFlow<AddStrainFormState> = _formState

    fun onOpenAddDialog() {
        _formState.value = AddStrainFormState(isDialogOpen = true)
    }

    fun onDismissDialog() {
        _formState.value = AddStrainFormState()
    }

    fun onNameChange(value: String) {
        _formState.value = _formState.value.copy(strainName = value, errorMessage = null)
    }

    fun onEggColorChange(value: EggColor) {
        _formState.value = _formState.value.copy(eggColor = value)
    }

    fun onInitialCountChange(value: String) {
        _formState.value = _formState.value.copy(initialChickCount = value, errorMessage = null)
    }

    fun onArrivalDateChange(value: LocalDate) {
        _formState.value = _formState.value.copy(arrivalDate = value)
    }

    fun onSave() {
        val state = _formState.value
        val count = state.initialChickCount.toIntOrNull()
        if (count == null) {
            _formState.value = state.copy(errorMessage = "عدد الكتاكيت يجب أن يكون رقماً صحيحاً")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isSaving = true, errorMessage = null)
            val strain = Strain(
                houseId = houseId,
                strainName = state.strainName,
                eggColor = state.eggColor,
                arrivalDate = state.arrivalDate,
                initialChickCount = count
            )
            addStrainUseCase(strain)
                .onSuccess { _formState.value = AddStrainFormState() }
                .onError { message -> _formState.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }
}

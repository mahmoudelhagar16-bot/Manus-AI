package com.farm.layermanager.ui.screens.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.usecase.house.AddHouseUseCase
import com.farm.layermanager.domain.usecase.house.DeactivateHouseUseCase
import com.farm.layermanager.domain.usecase.house.GetHousesUseCase
import com.farm.layermanager.domain.usecase.house.ReactivateHouseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HouseListUiState(
    val isAddDialogOpen: Boolean = false,
    val newHouseName: String = "",
    val newHouseNumber: String = "",
    val newHouseNotes: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class HouseListViewModel @Inject constructor(
    private val getHousesUseCase: GetHousesUseCase,
    private val addHouseUseCase: AddHouseUseCase,
    private val deactivateHouseUseCase: DeactivateHouseUseCase,
    private val reactivateHouseUseCase: ReactivateHouseUseCase
) : ViewModel() {

    val houses: StateFlow<List<House>> = getHousesUseCase.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(HouseListUiState())
    val uiState: StateFlow<HouseListUiState> = _uiState

    fun onOpenAddDialog() {
        _uiState.value = HouseListUiState(isAddDialogOpen = true)
    }

    fun onDismissAddDialog() {
        _uiState.value = HouseListUiState()
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(newHouseName = value, errorMessage = null)
    }

    fun onNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(newHouseNumber = value, errorMessage = null)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(newHouseNotes = value)
    }

    fun onSaveHouse() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            val result = addHouseUseCase(
                name = state.newHouseName,
                number = state.newHouseNumber,
                notes = state.newHouseNotes.ifBlank { null }
            )
            result
                .onSuccess { _uiState.value = HouseListUiState() }
                .onError { message -> _uiState.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }

    fun onToggleActive(house: House) {
        viewModelScope.launch {
            if (house.status.name == "ACTIVE") {
                deactivateHouseUseCase(house.houseId)
            } else {
                reactivateHouseUseCase(house.houseId)
            }
        }
    }
}

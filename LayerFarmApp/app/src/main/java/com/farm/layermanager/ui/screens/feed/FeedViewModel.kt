package com.farm.layermanager.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.FeedConsumption
import com.farm.layermanager.domain.model.FeedType
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.usecase.feed.AddFeedPurchaseUseCase
import com.farm.layermanager.domain.usecase.feed.AddFeedTypeUseCase
import com.farm.layermanager.domain.usecase.feed.GetFeedUseCase
import com.farm.layermanager.domain.usecase.feed.RecordFeedConsumptionUseCase
import com.farm.layermanager.domain.usecase.house.GetHousesUseCase
import com.farm.layermanager.domain.usecase.strain.GetStrainDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FeedTypeFormState(
    val isDialogOpen: Boolean = false,
    val feedName: String = "",
    val company: String = "",
    val price: String = "",
    val bagWeightKg: String = "",
    val quantityKg: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

data class ConsumptionFormState(
    val isDialogOpen: Boolean = false,
    val feedTypeId: Long? = null,
    val selectedHouseId: Long? = null,
    val selectedStrainId: Long? = null,
    val bagsCount: String = "",
    val totalWeightKg: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase,
    private val addFeedTypeUseCase: AddFeedTypeUseCase,
    private val addFeedPurchaseUseCase: AddFeedPurchaseUseCase,
    private val recordFeedConsumptionUseCase: RecordFeedConsumptionUseCase,
    private val getHousesUseCase: GetHousesUseCase,
    private val getStrainDetailsUseCase: GetStrainDetailsUseCase
) : ViewModel() {

    val feedTypes: StateFlow<List<FeedType>> = getFeedUseCase.getFeedTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val houses: StateFlow<List<House>> = getHousesUseCase.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flow خام (وليس StateFlow) — لا يُنشئ مُجمِّعاً دائماً في viewModelScope عند كل استدعاء من الواجهة. */
    fun strainsForHouse(houseId: Long): Flow<List<Strain>> = getStrainDetailsUseCase.getByHouse(houseId)

    private val _typeFormState = MutableStateFlow(FeedTypeFormState())
    val typeFormState: StateFlow<FeedTypeFormState> = _typeFormState

    private val _consumptionFormState = MutableStateFlow(ConsumptionFormState())
    val consumptionFormState: StateFlow<ConsumptionFormState> = _consumptionFormState

    fun onOpenAddTypeDialog() { _typeFormState.value = FeedTypeFormState(isDialogOpen = true) }
    fun onDismissTypeDialog() { _typeFormState.value = FeedTypeFormState() }
    fun onTypeFieldChange(update: FeedTypeFormState.() -> FeedTypeFormState) {
        _typeFormState.value = _typeFormState.value.update().copy(errorMessage = null)
    }

    fun onSaveFeedType() {
        val state = _typeFormState.value
        val price = state.price.toDoubleOrNull()
        val bagWeight = state.bagWeightKg.toDoubleOrNull()
        val qty = state.quantityKg.toDoubleOrNull()
        if (price == null || bagWeight == null || qty == null) {
            _typeFormState.value = state.copy(errorMessage = "الرجاء تعبئة السعر ووزن الشيكارة والكمية بأرقام صحيحة")
            return
        }
        viewModelScope.launch {
            _typeFormState.value = state.copy(isSaving = true, errorMessage = null)
            val feedType = FeedType(
                feedName = state.feedName,
                company = state.company.ifBlank { null },
                price = price,
                bagWeightKg = bagWeight,
                purchaseDate = LocalDate.now(),
                quantityKg = qty,
                currentStockKg = qty,
                weightedAvgCost = price
            )
            addFeedTypeUseCase(feedType)
                .onSuccess { _typeFormState.value = FeedTypeFormState() }
                .onError { message -> _typeFormState.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }

    fun onOpenConsumptionDialog(feedTypeId: Long) {
        _consumptionFormState.value = ConsumptionFormState(isDialogOpen = true, feedTypeId = feedTypeId)
    }
    fun onDismissConsumptionDialog() { _consumptionFormState.value = ConsumptionFormState() }
    fun onConsumptionFieldChange(update: ConsumptionFormState.() -> ConsumptionFormState) {
        _consumptionFormState.value = _consumptionFormState.value.update().copy(errorMessage = null)
    }

    fun onConsumptionHouseSelected(houseId: Long) {
        _consumptionFormState.value = _consumptionFormState.value.copy(
            selectedHouseId = houseId, selectedStrainId = null, errorMessage = null
        )
    }

    fun onConsumptionStrainSelected(strainId: Long) {
        _consumptionFormState.value = _consumptionFormState.value.copy(selectedStrainId = strainId, errorMessage = null)
    }

    fun onSaveConsumption() {
        val state = _consumptionFormState.value
        val feedTypeId = state.feedTypeId
        val houseId = state.selectedHouseId
        val strainId = state.selectedStrainId
        val bags = state.bagsCount.toDoubleOrNull() ?: 0.0
        val weight = state.totalWeightKg.toDoubleOrNull()

        if (feedTypeId == null || houseId == null || strainId == null || weight == null) {
            _consumptionFormState.value = state.copy(errorMessage = "الرجاء اختيار العنبر والسلالة وإدخال كمية صحيحة")
            return
        }

        viewModelScope.launch {
            _consumptionFormState.value = state.copy(isSaving = true, errorMessage = null)
            val feedType = feedTypes.value.firstOrNull { it.feedTypeId == feedTypeId }
            val cost = weight * (feedType?.weightedAvgCost ?: 0.0)
            val consumption = FeedConsumption(
                cDate = LocalDate.now(),
                houseId = houseId,
                strainId = strainId,
                feedTypeId = feedTypeId,
                bagsCount = bags,
                totalWeightKg = weight,
                cost = cost
            )
            recordFeedConsumptionUseCase(consumption)
                .onSuccess { _consumptionFormState.value = ConsumptionFormState() }
                .onError { message -> _consumptionFormState.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }
}

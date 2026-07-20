package com.farm.layermanager.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.InventoryCategory
import com.farm.layermanager.domain.model.InventoryItem
import com.farm.layermanager.domain.model.InventoryTransaction
import com.farm.layermanager.domain.model.InventoryTransactionType
import com.farm.layermanager.domain.usecase.inventory.AddInventoryItemUseCase
import com.farm.layermanager.domain.usecase.inventory.GetInventoryUseCase
import com.farm.layermanager.domain.usecase.inventory.RecordInventoryTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class InventoryItemFormState(
    val isDialogOpen: Boolean = false,
    val itemName: String = "",
    val category: InventoryCategory = InventoryCategory.MEDICINE,
    val unit: String = "",
    val minThreshold: String = "0",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

data class TransactionFormState(
    val isDialogOpen: Boolean = false,
    val itemId: Long? = null,
    val type: InventoryTransactionType = InventoryTransactionType.OUT,
    val quantity: String = "",
    val notes: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getInventoryUseCase: GetInventoryUseCase,
    private val addInventoryItemUseCase: AddInventoryItemUseCase,
    private val recordInventoryTransactionUseCase: RecordInventoryTransactionUseCase
) : ViewModel() {

    val items: StateFlow<List<InventoryItem>> = getInventoryUseCase.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _itemForm = MutableStateFlow(InventoryItemFormState())
    val itemForm: StateFlow<InventoryItemFormState> = _itemForm

    private val _transactionForm = MutableStateFlow(TransactionFormState())
    val transactionForm: StateFlow<TransactionFormState> = _transactionForm

    fun onOpenAddItemDialog() { _itemForm.value = InventoryItemFormState(isDialogOpen = true) }
    fun onDismissAddItemDialog() { _itemForm.value = InventoryItemFormState() }
    fun onItemFieldChange(update: InventoryItemFormState.() -> InventoryItemFormState) {
        _itemForm.value = _itemForm.value.update().copy(errorMessage = null)
    }

    fun onSaveItem() {
        val state = _itemForm.value
        viewModelScope.launch {
            _itemForm.value = state.copy(isSaving = true, errorMessage = null)
            val item = InventoryItem(
                category = state.category,
                itemName = state.itemName,
                unit = state.unit,
                currentStock = 0.0,
                minThreshold = state.minThreshold.toDoubleOrNull() ?: 0.0
            )
            addInventoryItemUseCase(item)
                .onSuccess { _itemForm.value = InventoryItemFormState() }
                .onError { message -> _itemForm.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }

    fun onOpenTransactionDialog(itemId: Long, type: InventoryTransactionType) {
        _transactionForm.value = TransactionFormState(isDialogOpen = true, itemId = itemId, type = type)
    }
    fun onDismissTransactionDialog() { _transactionForm.value = TransactionFormState() }
    fun onTransactionFieldChange(update: TransactionFormState.() -> TransactionFormState) {
        _transactionForm.value = _transactionForm.value.update().copy(errorMessage = null)
    }

    fun onSaveTransaction() {
        val state = _transactionForm.value
        val itemId = state.itemId
        val quantity = state.quantity.toDoubleOrNull()
        if (itemId == null || quantity == null) {
            _transactionForm.value = state.copy(errorMessage = "الرجاء إدخال كمية صحيحة")
            return
        }
        viewModelScope.launch {
            _transactionForm.value = state.copy(isSaving = true, errorMessage = null)
            val transaction = InventoryTransaction(
                itemId = itemId,
                tDate = LocalDate.now(),
                type = state.type,
                quantity = quantity,
                notes = state.notes.ifBlank { null }
            )
            recordInventoryTransactionUseCase(transaction)
                .onSuccess { _transactionForm.value = TransactionFormState() }
                .onError { message -> _transactionForm.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }
}

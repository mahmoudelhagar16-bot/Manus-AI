package com.farm.layermanager.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.Customer
import com.farm.layermanager.domain.model.Sale
import com.farm.layermanager.domain.repository.EggInventoryBalance
import com.farm.layermanager.domain.usecase.sales.AddCustomerUseCase
import com.farm.layermanager.domain.usecase.sales.GetCustomersUseCase
import com.farm.layermanager.domain.usecase.sales.GetSalesUseCase
import com.farm.layermanager.domain.usecase.sales.RecordSaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SaleFormState(
    val isDialogOpen: Boolean = false,
    val customerId: Long? = null,
    val whiteTrays: String = "0",
    val redTrays: String = "0",
    val crackedTrays: String = "0",
    val whitePrice: String = "",
    val redPrice: String = "",
    val crackedPrice: String = "",
    val paidAmount: String = "0",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

data class CustomerFormState(
    val isDialogOpen: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val getSalesUseCase: GetSalesUseCase,
    private val getCustomersUseCase: GetCustomersUseCase,
    private val addCustomerUseCase: AddCustomerUseCase,
    private val recordSaleUseCase: RecordSaleUseCase
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = getCustomersUseCase.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSales: StateFlow<List<Sale>> = getSalesUseCase.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eggBalance: StateFlow<EggInventoryBalance?> = getSalesUseCase.getEggInventoryBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val outstandingDebt: StateFlow<Double> = getSalesUseCase.getTotalOutstandingDebt()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _saleForm = MutableStateFlow(SaleFormState())
    val saleForm: StateFlow<SaleFormState> = _saleForm

    private val _customerForm = MutableStateFlow(CustomerFormState())
    val customerForm: StateFlow<CustomerFormState> = _customerForm

    fun onOpenSaleDialog(customerId: Long) { _saleForm.value = SaleFormState(isDialogOpen = true, customerId = customerId) }
    fun onDismissSaleDialog() { _saleForm.value = SaleFormState() }
    fun onSaleFieldChange(update: SaleFormState.() -> SaleFormState) {
        _saleForm.value = _saleForm.value.update().copy(errorMessage = null)
    }

    fun onSaveSale() {
        val state = _saleForm.value
        val customerId = state.customerId
        val white = state.whiteTrays.toDoubleOrNull() ?: 0.0
        val red = state.redTrays.toDoubleOrNull() ?: 0.0
        val cracked = state.crackedTrays.toDoubleOrNull() ?: 0.0
        val whitePrice = state.whitePrice.toDoubleOrNull() ?: 0.0
        val redPrice = state.redPrice.toDoubleOrNull() ?: 0.0
        val crackedPrice = state.crackedPrice.toDoubleOrNull() ?: 0.0
        val paid = state.paidAmount.toDoubleOrNull() ?: 0.0

        if (customerId == null) {
            _saleForm.value = state.copy(errorMessage = "الرجاء اختيار عميل")
            return
        }

        viewModelScope.launch {
            _saleForm.value = state.copy(isSaving = true, errorMessage = null)
            recordSaleUseCase(
                customerId = customerId,
                sDate = LocalDate.now(),
                whiteTrays = white,
                redTrays = red,
                crackedTrays = cracked,
                whitePrice = whitePrice,
                redPrice = redPrice,
                crackedPrice = crackedPrice,
                paidAmount = paid,
                paymentMethod = null,
                notes = null
            )
                .onSuccess { _saleForm.value = SaleFormState() }
                .onError { message -> _saleForm.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }

    fun onOpenCustomerDialog() { _customerForm.value = CustomerFormState(isDialogOpen = true) }
    fun onDismissCustomerDialog() { _customerForm.value = CustomerFormState() }
    fun onCustomerFieldChange(update: CustomerFormState.() -> CustomerFormState) {
        _customerForm.value = _customerForm.value.update().copy(errorMessage = null)
    }

    fun onSaveCustomer() {
        val state = _customerForm.value
        viewModelScope.launch {
            _customerForm.value = state.copy(isSaving = true, errorMessage = null)
            val customer = Customer(name = state.name, phone = state.phone.ifBlank { null })
            addCustomerUseCase(customer)
                .onSuccess { _customerForm.value = CustomerFormState() }
                .onError { message -> _customerForm.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }
}

package com.farm.layermanager.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farm.layermanager.domain.model.Expense
import com.farm.layermanager.domain.model.ExpenseCategory
import com.farm.layermanager.domain.model.Revenue
import com.farm.layermanager.domain.usecase.finance.AddExpenseUseCase
import com.farm.layermanager.domain.usecase.finance.AddRevenueUseCase
import com.farm.layermanager.domain.usecase.finance.GetExpenseCategoriesUseCase
import com.farm.layermanager.domain.usecase.finance.GetFinanceUseCase
import com.farm.layermanager.domain.usecase.finance.GetNetProfitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExpenseFormState(
    val isDialogOpen: Boolean = false,
    val categoryId: Long? = null,
    val amount: String = "",
    val description: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

data class RevenueFormState(
    val isDialogOpen: Boolean = false,
    val revenueTypeId: Long? = null,
    val amount: String = "",
    val description: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val getFinanceUseCase: GetFinanceUseCase,
    private val getNetProfitUseCase: GetNetProfitUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val addRevenueUseCase: AddRevenueUseCase,
    private val getExpenseCategoriesUseCase: GetExpenseCategoriesUseCase
) : ViewModel() {

    private val monthStart = LocalDate.now().withDayOfMonth(1)
    private val today = LocalDate.now()

    val categories: StateFlow<List<ExpenseCategory>> = getExpenseCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesThisMonth: StateFlow<List<Expense>> = getFinanceUseCase.getExpensesInRange(monthStart, today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revenuesThisMonth: StateFlow<List<Revenue>> = getFinanceUseCase.getRevenuesInRange(monthStart, today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpensesThisMonth: StateFlow<Double> = getFinanceUseCase.getTotalExpensesInRange(monthStart, today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netProfitThisMonth: StateFlow<Double> = getNetProfitUseCase(monthStart, today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _expenseForm = MutableStateFlow(ExpenseFormState())
    val expenseForm: StateFlow<ExpenseFormState> = _expenseForm

    fun onOpenExpenseDialog() { _expenseForm.value = ExpenseFormState(isDialogOpen = true) }
    fun onDismissExpenseDialog() { _expenseForm.value = ExpenseFormState() }
    fun onExpenseFieldChange(update: ExpenseFormState.() -> ExpenseFormState) {
        _expenseForm.value = _expenseForm.value.update().copy(errorMessage = null)
    }

    fun onSaveExpense() {
        val state = _expenseForm.value
        val categoryId = state.categoryId
        val amount = state.amount.toDoubleOrNull()
        if (categoryId == null || amount == null) {
            _expenseForm.value = state.copy(errorMessage = "الرجاء اختيار فئة وإدخال مبلغ صحيح")
            return
        }
        viewModelScope.launch {
            _expenseForm.value = state.copy(isSaving = true, errorMessage = null)
            val expense = Expense(
                eDate = LocalDate.now(),
                categoryId = categoryId,
                houseId = null,
                amount = amount,
                description = state.description.ifBlank { null }
            )
            addExpenseUseCase(expense)
                .onSuccess { _expenseForm.value = ExpenseFormState() }
                .onError { message -> _expenseForm.value = state.copy(isSaving = false, errorMessage = message) }
        }
    }
}

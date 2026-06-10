package com.muhsinov.budget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhsinov.budget.data.model.Budget
import com.muhsinov.budget.data.model.BudgetWithCategory
import com.muhsinov.budget.data.model.Category
import com.muhsinov.budget.data.model.CategoryType
import com.muhsinov.budget.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val budgets: List<BudgetWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val currentMonthYear: String = "",
    val isLoading: Boolean = true
)

data class AddBudgetUiState(
    val selectedCategoryId: Long? = null,
    val limitAmount: String = "",
    val availableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val currentMonthYear = repository.getCurrentMonthYear()

    val uiState: StateFlow<BudgetUiState> = combine(
        repository.getBudgetsWithCategoryForMonth(currentMonthYear),
        repository.getCategoriesByType(CategoryType.EXPENSE)
    ) { budgets, categories ->
        BudgetUiState(
            budgets = budgets,
            categories = categories,
            currentMonthYear = currentMonthYear,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState(currentMonthYear = currentMonthYear)
    )

    private val _addBudgetState = MutableStateFlow(AddBudgetUiState())
    val addBudgetState: StateFlow<AddBudgetUiState> = _addBudgetState

    init {
        viewModelScope.launch {
            repository.getCategoriesByType(CategoryType.EXPENSE).collect { categories ->
                _addBudgetState.value = _addBudgetState.value.copy(availableCategories = categories)
            }
        }
    }

    fun loadBudgetForEdit(budgetId: Long) {
        viewModelScope.launch {
            val budget = repository.getBudgetById(budgetId) ?: return@launch
            _addBudgetState.value = _addBudgetState.value.copy(
                selectedCategoryId = budget.categoryId,
                limitAmount = budget.limitAmount.toBigDecimal().stripTrailingZeros().toPlainString(),
                isSaved = false,
                errorMessage = null
            )
        }
    }

    fun updateCategory(categoryId: Long) {
        _addBudgetState.value = _addBudgetState.value.copy(
            selectedCategoryId = categoryId,
            errorMessage = null
        )
    }

    fun updateLimitAmount(amount: String) {
        _addBudgetState.value = _addBudgetState.value.copy(
            limitAmount = amount,
            errorMessage = null
        )
    }

    fun saveBudget(existingId: Long? = null) {
        val state = _addBudgetState.value
        val limit = state.limitAmount.toDoubleOrNull()

        if (limit == null || limit <= 0.0) {
            _addBudgetState.value = state.copy(errorMessage = "Please enter a valid amount")
            return
        }
        if (state.selectedCategoryId == null) {
            _addBudgetState.value = state.copy(errorMessage = "Please select a category")
            return
        }

        viewModelScope.launch {
            _addBudgetState.value = _addBudgetState.value.copy(isLoading = true)

            if (existingId != null) {
                val existing = repository.getBudgetById(existingId)
                existing?.let {
                    repository.updateBudget(it.copy(
                        categoryId = state.selectedCategoryId,
                        limitAmount = limit
                    ))
                }
            } else {
                val alreadyExists = repository.getBudgetByCategoryAndMonth(
                    state.selectedCategoryId, currentMonthYear
                )
                if (alreadyExists != null) {
                    repository.updateBudget(alreadyExists.copy(limitAmount = limit))
                } else {
                    val spent = repository.getSpentForCategory(state.selectedCategoryId, currentMonthYear)
                    repository.insertBudget(
                        Budget(
                            categoryId = state.selectedCategoryId,
                            monthYear = currentMonthYear,
                            limitAmount = limit,
                            spent = spent
                        )
                    )
                }
            }

            _addBudgetState.value = _addBudgetState.value.copy(isLoading = false, isSaved = true)
        }
    }

    fun resetAddBudgetForm() {
        _addBudgetState.value = AddBudgetUiState(
            availableCategories = _addBudgetState.value.availableCategories
        )
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun refreshSpent() {
        viewModelScope.launch {
            val budgets = uiState.value.budgets
            for (bwc in budgets) {
                val spent = repository.getSpentForCategory(bwc.budget.categoryId, currentMonthYear)
                repository.updateBudgetSpent(bwc.budget.id, spent)
            }
        }
    }
}

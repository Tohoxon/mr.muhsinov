package com.muhsinov.budget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhsinov.budget.data.model.Category
import com.muhsinov.budget.data.model.Transaction
import com.muhsinov.budget.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategorySpending(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val (startOfMonth, endOfMonth) = repository.getCurrentMonthRange()

        viewModelScope.launch {
            combine(
                repository.getAllCategories(),
                repository.getMonthlyIncome(startOfMonth, endOfMonth),
                repository.getMonthlyExpenses(startOfMonth, endOfMonth),
                repository.getRecentTransactions(10),
                repository.getMonthlyCategoryExpenses(startOfMonth, endOfMonth)
            ) { categories, income, expenses, recent, categorySums ->
                val categoryMap = categories.associateBy { it.id }
                val monthlyIncome = income ?: 0.0
                val monthlyExpenses = expenses ?: 0.0
                val totalBalance = monthlyIncome - monthlyExpenses

                val breakdown = categorySums.mapNotNull { sum ->
                    val category = sum.categoryId?.let { categoryMap[it] } ?: return@mapNotNull null
                    val percentage = if (monthlyExpenses > 0) {
                        (sum.total / monthlyExpenses * 100).toFloat()
                    } else 0f
                    CategorySpending(category, sum.total, percentage)
                }.sortedByDescending { it.amount }

                DashboardUiState(
                    totalBalance = totalBalance,
                    monthlyIncome = monthlyIncome,
                    monthlyExpenses = monthlyExpenses,
                    recentTransactions = recent,
                    categoryBreakdown = breakdown,
                    categories = categoryMap,
                    isLoading = false
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                DashboardUiState()
            ).collect { state ->
                _uiState.value = state
            }
        }
    }
}

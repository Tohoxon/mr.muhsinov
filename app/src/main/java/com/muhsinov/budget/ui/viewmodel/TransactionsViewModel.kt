package com.muhsinov.budget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhsinov.budget.data.model.Category
import com.muhsinov.budget.data.model.Transaction
import com.muhsinov.budget.data.model.TransactionType
import com.muhsinov.budget.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TransactionFilter { ALL, INCOME, EXPENSE }

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val filter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

data class AddTransactionUiState(
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: TransactionType = TransactionType.EXPENSE,
    val availableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories(),
        _filter,
        _searchQuery
    ) { transactions, categories, filter, query ->
        val categoryMap = categories.associateBy { it.id }
        val filtered = transactions
            .filter { t ->
                when (filter) {
                    TransactionFilter.ALL -> true
                    TransactionFilter.INCOME -> t.type == TransactionType.INCOME
                    TransactionFilter.EXPENSE -> t.type == TransactionType.EXPENSE
                }
            }
            .filter { t ->
                if (query.isBlank()) true
                else {
                    val cat = t.categoryId?.let { categoryMap[it] }
                    t.note.contains(query, ignoreCase = true) ||
                        cat?.name?.contains(query, ignoreCase = true) == true
                }
            }
        TransactionsUiState(
            transactions = filtered,
            categories = categoryMap,
            filter = filter,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )

    // Add/Edit Transaction state
    private val _addState = MutableStateFlow(AddTransactionUiState())
    val addTransactionState: StateFlow<AddTransactionUiState> = _addState

    init {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _addState.value = _addState.value.copy(availableCategories = categories)
            }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun loadTransactionForEdit(transactionId: Long) {
        viewModelScope.launch {
            val t = repository.getTransactionById(transactionId) ?: return@launch
            _addState.value = _addState.value.copy(
                amount = t.amount.toBigDecimal().stripTrailingZeros().toPlainString(),
                selectedCategoryId = t.categoryId,
                note = t.note,
                date = t.date,
                type = t.type,
                isSaved = false,
                errorMessage = null
            )
        }
    }

    fun updateAmount(amount: String) {
        _addState.value = _addState.value.copy(amount = amount, errorMessage = null)
    }

    fun updateCategory(categoryId: Long) {
        _addState.value = _addState.value.copy(selectedCategoryId = categoryId)
    }

    fun updateNote(note: String) {
        _addState.value = _addState.value.copy(note = note)
    }

    fun updateDate(date: Long) {
        _addState.value = _addState.value.copy(date = date)
    }

    fun updateType(type: TransactionType) {
        _addState.value = _addState.value.copy(type = type, selectedCategoryId = null)
    }

    fun saveTransaction(existingId: Long? = null) {
        val state = _addState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _addState.value = state.copy(errorMessage = "Please enter a valid amount")
            return
        }
        if (state.selectedCategoryId == null) {
            _addState.value = state.copy(errorMessage = "Please select a category")
            return
        }

        viewModelScope.launch {
            _addState.value = _addState.value.copy(isLoading = true)
            val transaction = Transaction(
                id = existingId ?: 0L,
                amount = amount,
                categoryId = state.selectedCategoryId,
                note = state.note.trim(),
                date = state.date,
                type = state.type
            )
            if (existingId != null) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }
            _addState.value = _addState.value.copy(isLoading = false, isSaved = true)
        }
    }

    fun resetForm() {
        _addState.value = AddTransactionUiState(
            availableCategories = _addState.value.availableCategories
        )
    }
}

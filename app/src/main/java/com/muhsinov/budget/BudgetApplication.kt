package com.muhsinov.budget

import android.app.Application
import com.muhsinov.budget.data.model.Category
import com.muhsinov.budget.data.model.CategoryType
import com.muhsinov.budget.data.repository.BudgetRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BudgetApplication : Application() {

    @Inject
    lateinit var repository: BudgetRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            seedDefaultCategories()
        }
    }

    private suspend fun seedDefaultCategories() {
        if (repository.getCategoryCount() > 0) return

        val defaultCategories = listOf(
            Category(name = "Food & Dining", icon = "🍔", color = "#FF5722", type = CategoryType.EXPENSE),
            Category(name = "Transport", icon = "🚗", color = "#2196F3", type = CategoryType.EXPENSE),
            Category(name = "Shopping", icon = "🛍️", color = "#E91E63", type = CategoryType.EXPENSE),
            Category(name = "Health", icon = "💊", color = "#4CAF50", type = CategoryType.EXPENSE),
            Category(name = "Entertainment", icon = "🎮", color = "#9C27B0", type = CategoryType.EXPENSE),
            Category(name = "Bills & Utilities", icon = "💡", color = "#FF9800", type = CategoryType.EXPENSE),
            Category(name = "Education", icon = "📚", color = "#00BCD4", type = CategoryType.EXPENSE),
            Category(name = "Salary", icon = "💼", color = "#00897B", type = CategoryType.INCOME),
            Category(name = "Freelance", icon = "💻", color = "#43A047", type = CategoryType.INCOME),
            Category(name = "Investment", icon = "📈", color = "#1E88E5", type = CategoryType.INCOME),
            Category(name = "Other Income", icon = "💰", color = "#FDD835", type = CategoryType.INCOME),
            Category(name = "Other", icon = "📦", color = "#78909C", type = CategoryType.BOTH)
        )

        repository.insertCategories(defaultCategories)
    }
}

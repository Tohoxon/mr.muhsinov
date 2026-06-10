package com.muhsinov.budget.data.repository

import com.muhsinov.budget.data.database.BudgetDao
import com.muhsinov.budget.data.database.CategoryDao
import com.muhsinov.budget.data.database.CategorySum
import com.muhsinov.budget.data.database.TransactionDao
import com.muhsinov.budget.data.model.Budget
import com.muhsinov.budget.data.model.BudgetWithCategory
import com.muhsinov.budget.data.model.Category
import com.muhsinov.budget.data.model.CategoryType
import com.muhsinov.budget.data.model.Transaction
import com.muhsinov.budget.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {

    // ---- Transactions ----

    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.delete(transaction)

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(type)

    suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)

    fun getRecentTransactions(limit: Int = 5): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit)

    fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsForMonth(startOfMonth, endOfMonth)

    fun getMonthlyIncome(startOfMonth: Long, endOfMonth: Long): Flow<Double?> =
        transactionDao.getSumByTypeForMonth(TransactionType.INCOME, startOfMonth, endOfMonth)

    fun getMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Flow<Double?> =
        transactionDao.getSumByTypeForMonth(TransactionType.EXPENSE, startOfMonth, endOfMonth)

    fun getMonthlyCategoryExpenses(startOfMonth: Long, endOfMonth: Long): Flow<List<CategorySum>> =
        transactionDao.getSumByCategoryForMonth(TransactionType.EXPENSE, startOfMonth, endOfMonth)

    // ---- Categories ----

    suspend fun insertCategory(category: Category): Long =
        categoryDao.insert(category)

    suspend fun insertCategories(categories: List<Category>) =
        categoryDao.insertAll(categories)

    suspend fun updateCategory(category: Category) =
        categoryDao.update(category)

    suspend fun deleteCategory(category: Category) =
        categoryDao.delete(category)

    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories()

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)

    suspend fun getCategoryCount(): Int =
        categoryDao.getCategoryCount()

    // ---- Budgets ----

    suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insert(budget)

    suspend fun updateBudget(budget: Budget) =
        budgetDao.update(budget)

    suspend fun deleteBudget(budget: Budget) =
        budgetDao.delete(budget)

    fun getBudgetsWithCategoryForMonth(monthYear: String): Flow<List<BudgetWithCategory>> =
        budgetDao.getBudgetsWithCategoryForMonth(monthYear)

    suspend fun getBudgetById(id: Long): Budget? =
        budgetDao.getBudgetById(id)

    suspend fun getBudgetByCategoryAndMonth(categoryId: Long, monthYear: String): Budget? =
        budgetDao.getBudgetByCategoryAndMonth(categoryId, monthYear)

    suspend fun updateBudgetSpent(budgetId: Long, spent: Double) =
        budgetDao.updateSpent(budgetId, spent)

    fun getAllBudgetsWithCategory(): Flow<List<BudgetWithCategory>> =
        budgetDao.getAllBudgetsWithCategory()

    // ---- Helpers ----

    fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    fun getCurrentMonthYear(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${String.format("%02d", cal.get(Calendar.MONTH) + 1)}"
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return getMonthRange(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    private fun parseMonthYear(monthYear: String): Pair<Long, Long> {
        val parts = monthYear.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        return getMonthRange(year, month)
    }

    suspend fun getSpentForCategory(categoryId: Long, monthYear: String): Double {
        val (start, end) = parseMonthYear(monthYear)
        return transactionDao.getSpentAmountForCategory(categoryId, start, end) ?: 0.0
    }
}

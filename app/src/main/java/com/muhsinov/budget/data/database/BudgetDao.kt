package com.muhsinov.budget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.muhsinov.budget.data.model.Budget
import com.muhsinov.budget.data.model.BudgetWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Transaction
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear ORDER BY categoryId ASC")
    fun getBudgetsWithCategoryForMonth(monthYear: String): Flow<List<BudgetWithCategory>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear ORDER BY categoryId ASC")
    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND monthYear = :monthYear")
    suspend fun getBudgetByCategoryAndMonth(categoryId: Long, monthYear: String): Budget?

    @Query("UPDATE budgets SET spent = :spent WHERE id = :budgetId")
    suspend fun updateSpent(budgetId: Long, spent: Double)

    @Transaction
    @Query("SELECT * FROM budgets ORDER BY monthYear DESC, categoryId ASC")
    fun getAllBudgetsWithCategory(): Flow<List<BudgetWithCategory>>
}

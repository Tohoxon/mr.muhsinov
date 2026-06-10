package com.muhsinov.budget.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.muhsinov.budget.data.model.Transaction
import com.muhsinov.budget.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

data class CategorySum(
    val categoryId: Long?,
    val total: Double
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("""
        SELECT * FROM transactions
        WHERE date >= :startOfMonth AND date <= :endOfMonth
        ORDER BY date DESC, createdAt DESC
    """)
    fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE type = :type AND date >= :startOfMonth AND date <= :endOfMonth
    """)
    fun getSumByTypeForMonth(
        type: TransactionType,
        startOfMonth: Long,
        endOfMonth: Long
    ): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(amount) as total FROM transactions
        WHERE type = :type AND date >= :startOfMonth AND date <= :endOfMonth
        GROUP BY categoryId
    """)
    fun getSumByCategoryForMonth(
        type: TransactionType,
        startOfMonth: Long,
        endOfMonth: Long
    ): Flow<List<CategorySum>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE categoryId = :categoryId AND type = 'EXPENSE'
        AND date >= :startOfMonth AND date <= :endOfMonth
    """)
    suspend fun getSpentAmountForCategory(
        categoryId: Long,
        startOfMonth: Long,
        endOfMonth: Long
    ): Double?

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 5): Flow<List<Transaction>>
}

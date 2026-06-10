package com.muhsinov.budget.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.muhsinov.budget.data.model.Budget
import com.muhsinov.budget.data.model.Category
import com.muhsinov.budget.data.model.CategoryType
import com.muhsinov.budget.data.model.Transaction
import com.muhsinov.budget.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)
}

@Database(
    entities = [Transaction::class, Category::class, Budget::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "budget_db"
    }
}

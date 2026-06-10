package com.muhsinov.budget.ui.screens.transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhsinov.budget.data.model.TransactionType
import com.muhsinov.budget.ui.theme.ExpenseRed
import com.muhsinov.budget.ui.theme.IncomeGreen
import com.muhsinov.budget.ui.viewmodel.TransactionsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.addTransactionState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransactionForEdit(transactionId)
        } else {
            viewModel.resetForm()
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (transactionId != null) "Edit Transaction" else "Add Transaction",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.type == type,
                        onClick = { viewModel.updateType(type) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TransactionType.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text(if (type == TransactionType.INCOME) "Income" else "Expense")
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::updateAmount,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.errorMessage?.contains("amount", ignoreCase = true) == true,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category
            Column {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.availableCategories
                        .filter { cat ->
                            when (state.type) {
                                TransactionType.INCOME -> cat.type.name != "EXPENSE"
                                TransactionType.EXPENSE -> cat.type.name != "INCOME"
                            }
                        }
                        .forEach { category ->
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(category.color))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            val isSelected = state.selectedCategoryId == category.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) catColor else catColor.copy(alpha = 0.1f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) catColor else catColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.updateCategory(category.id) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(category.icon, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        category.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                }
            }

            // Note
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::updateNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Date
            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(state.date))
            OutlinedTextField(
                value = dateStr,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = state.date }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val newCal = Calendar.getInstance()
                                newCal.set(year, month, day)
                                viewModel.updateDate(newCal.timeInMillis)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                label = { Text("Date") },
                trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            if (state.errorMessage != null) {
                Text(
                    state.errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveTransaction(transactionId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        if (transactionId != null) "Update Transaction" else "Save Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

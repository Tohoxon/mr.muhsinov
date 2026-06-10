package com.muhsinov.budget.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhsinov.budget.data.model.BudgetWithCategory
import com.muhsinov.budget.ui.theme.ExpenseRed
import com.muhsinov.budget.ui.theme.IncomeGreen
import com.muhsinov.budget.ui.theme.WarningAmber
import com.muhsinov.budget.ui.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetScreen(
    onAddBudget: () -> Unit,
    onEditBudget: (Long) -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshSpent()
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBudget,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Budget") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Text(
                "Monthly Budget",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (state.budgets.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💰", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No budgets set",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Tap + to set a monthly spending limit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(state.budgets, key = { it.budget.id }) { bwc ->
                        BudgetCard(
                            budgetWithCategory = bwc,
                            onEdit = { onEditBudget(bwc.budget.id) },
                            onDelete = { viewModel.deleteBudget(bwc.budget) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(
    budgetWithCategory: BudgetWithCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val budget = budgetWithCategory.budget
    val category = budgetWithCategory.category
    val progress = if (budget.limitAmount > 0) (budget.spent / budget.limitAmount).toFloat() else 0f
    val isExceeded = progress > 1f
    val isWarning = progress > 0.8f && !isExceeded

    val catColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val progressColor = when {
        isExceeded -> ExpenseRed
        isWarning -> WarningAmber
        else -> catColor
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(category.icon, style = MaterialTheme.typography.titleMedium)
                    }
                    Column {
                        Text(category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        if (isExceeded) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Warning, null, tint = ExpenseRed, modifier = Modifier.size(12.dp))
                                Text(
                                    "Budget exceeded!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp), tint = ExpenseRed)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatCurrency(budget.spent),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExceeded) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatCurrency(budget.limitAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f)
            )

            Spacer(Modifier.height(4.dp))

            val remaining = budget.limitAmount - budget.spent
            Text(
                if (remaining >= 0) "${formatCurrency(remaining)} remaining"
                else "${formatCurrency(-remaining)} over budget",
                style = MaterialTheme.typography.bodySmall,
                color = if (remaining < 0) ExpenseRed else IncomeGreen
            )
        }
    }
}

private fun formatCurrency(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)

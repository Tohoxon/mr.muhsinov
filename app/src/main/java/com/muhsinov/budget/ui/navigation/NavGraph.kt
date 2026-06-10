package com.muhsinov.budget.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.muhsinov.budget.ui.screens.budget.AddBudgetScreen
import com.muhsinov.budget.ui.screens.budget.BudgetScreen
import com.muhsinov.budget.ui.screens.dashboard.DashboardScreen
import com.muhsinov.budget.ui.screens.transactions.AddTransactionScreen
import com.muhsinov.budget.ui.screens.transactions.TransactionsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Long? = null): String =
            if (transactionId != null) "add_transaction?transactionId=$transactionId"
            else "add_transaction"
    }
    object Budget : Screen("budget")
    object AddBudget : Screen("add_budget?budgetId={budgetId}") {
        fun createRoute(budgetId: Long? = null): String =
            if (budgetId != null) "add_budget?budgetId=$budgetId"
            else "add_budget"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomNavItem(Screen.Transactions, "Transactions", Icons.Filled.List, Icons.Outlined.List),
    BottomNavItem(Screen.Budget, "Budget", Icons.Filled.PieChart, Icons.Outlined.PieChart)
)

val bottomNavRoutes = bottomNavItems.map { it.screen.route }

@Composable
fun BudgetNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavRoutes ||
        currentDestination?.route == Screen.Transactions.route

    val showFab = currentDestination?.route == Screen.Transactions.route ||
        currentDestination?.route == Screen.Dashboard.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp(0f)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddTransaction.createRoute()) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.createRoute())
                }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.AddTransaction.createRoute(transactionId))
                },
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.createRoute())
                }
            )
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId")
                ?.takeIf { it != -1L }
            AddTransactionScreen(
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Budget.route) {
            BudgetScreen(
                onAddBudget = {
                    navController.navigate(Screen.AddBudget.createRoute())
                },
                onEditBudget = { budgetId ->
                    navController.navigate(Screen.AddBudget.createRoute(budgetId))
                }
            )
        }

        composable(
            route = Screen.AddBudget.route,
            arguments = listOf(
                navArgument("budgetId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getLong("budgetId")
                ?.takeIf { it != -1L }
            AddBudgetScreen(
                budgetId = budgetId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

package com.example.subscriptiontracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.subscriptiontracker.SubscriptionViewModel

/** Экраны, доступные из нижней панели навигации. */
private enum class Tab(val route: String, val title: String, val icon: ImageVector) {
    Subscriptions("subscriptions", "Подписки", Icons.AutoMirrored.Filled.List),
    Payments("payments", "Платежи", Icons.Default.DateRange),
    Stats("stats", "Статистика", Icons.Default.PieChart)
}

private const val EDIT_ROUTE = "edit"

/** Корневая функция интерфейса: нижняя панель и граф навигации. */
@Composable
fun SubscriptionTrackerApp(vm: SubscriptionViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // Список подписок читается один раз здесь и передаётся всем экранам
    val subscriptions by vm.subscriptions.collectAsStateWithLifecycle()

    val openEditor: (Long) -> Unit = { id -> navController.navigate("$EDIT_ROUTE?id=$id") }

    Scaffold(
        bottomBar = {
            // На экране редактирования нижняя панель скрыта
            if (Tab.entries.any { it.route == currentRoute }) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Subscriptions.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Subscriptions.route) {
                SubscriptionsScreen(
                    subscriptions = subscriptions,
                    onAdd = { openEditor(-1L) },
                    onOpen = openEditor,
                    onAddSamples = vm::addSamples
                )
            }
            composable(Tab.Payments.route) {
                PaymentsScreen(subscriptions = subscriptions, onOpen = openEditor)
            }
            composable(Tab.Stats.route) {
                StatsScreen(subscriptions = subscriptions)
            }
            composable(
                route = "$EDIT_ROUTE?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { entry ->
                EditSubscriptionScreen(
                    id = entry.arguments?.getLong("id") ?: -1L,
                    loadSubscription = vm::getById,
                    onSave = vm::save,
                    onDelete = vm::delete,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/** Переход на вкладку без накопления копий экранов в стеке. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

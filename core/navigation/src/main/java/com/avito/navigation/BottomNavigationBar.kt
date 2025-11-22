package com.avito.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.avito.core.navigation.R

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentDestination: NavDestination?
) {
    NavigationBar {
        topLevelRoutes.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.name,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.name) },
                selected = currentDestination
                    ?.hierarchy
                    ?.any { it.hasRoute(item.route::class) } == true,
                onClick = {
                    // Не навигируем, если уже на этом экране
                    val isSelected = currentDestination
                        ?.hierarchy
                        ?.any { it.hasRoute(item.route::class) } == true
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo<ScreenRoute.BookList> {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

private val topLevelRoutes = listOf(
    TopLevelRoute("Книги", ScreenRoute.BookList, R.drawable.booklist),
    TopLevelRoute("Загрузить книгу", ScreenRoute.UploadBook, R.drawable.upload),
    TopLevelRoute("Профиль", ScreenRoute.Profile, R.drawable.account),
)

private data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val icon: Int
)

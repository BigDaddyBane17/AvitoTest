package com.avito.avitotest

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.avito.avitotest.R
import com.avito.avitotest.di.AppComponent
import com.avito.navigation.BottomNavigationBar
import com.avito.navigation.ScreenRoute
import com.avito.navigation.TopAppBar

@Composable
fun RootAppScreen(
    isAuthorized: Boolean,
    appComponent: AppComponent
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination

    val startDestination = if (isAuthorized) {
        ScreenRoute.BookList
    } else {
        ScreenRoute.Auth
    }

    val isAuthDestination = currentDestination
        ?.hierarchy
        ?.any { it.hasRoute(ScreenRoute.Auth::class) } == true

    Scaffold(
        topBar = {
            if (!isAuthDestination) {
                TopAppBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        },
        bottomBar = {
            if (!isAuthDestination) {
                BottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { padding ->
        val webClientId = stringResource(id = R.string.default_web_client_id)
        RootNavGraph(
            navController = navController,
            startDestination = startDestination,
            appComponent = appComponent,
            googleWebClientId = webClientId,
            modifier = Modifier.padding(padding),
        )
    }
}

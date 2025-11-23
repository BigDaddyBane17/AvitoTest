package com.avito.avitotest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.avito.avitotest.di.AppComponent
import com.avito.navigation.BottomNavigationBar
import com.avito.navigation.ScreenRoute
import com.avito.navigation.TopAppBar
import com.avito.navigation.TopBarConfig
import com.avito.ui.transition.standardFadeIn
import com.avito.ui.transition.standardFadeOut

@OptIn(ExperimentalAnimationApi::class)
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
    
    val isBookReaderDestination = currentDestination?.route?.contains("BookReaderRoute") == true
    
    var topBarConfig by remember { mutableStateOf<TopBarConfig?>(null) }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = topBarConfig != null,
                enter = standardFadeIn(),
                exit = standardFadeOut()
            ) {
                topBarConfig?.let { config ->
                    TopAppBar(
                        navController = navController,
                        config = config
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isAuthDestination && !isBookReaderDestination,
                enter = standardFadeIn(),
                exit = standardFadeOut()
            ) {
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
            onTopBarConfigChange = { config -> topBarConfig = config }
        )
    }
}

package com.avito.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.avito.core.navigation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavController,
    currentDestination: NavDestination?,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val titleRes = when {
        currentDestination.inHierarchy(typeRoute<ScreenRoute.BookList>()) ->
            R.string.title_books

        currentDestination.inHierarchy(typeRoute<ScreenRoute.UploadBook>()) ->
            R.string.title_upload

        currentDestination.inHierarchy(typeRoute<ScreenRoute.Profile>()) ->
            R.string.title_profile

        else -> R.string.app_name
    }

    CenterAlignedTopAppBar(
        navigationIcon = {
            val canNavigateBack =
                navController.previousBackStackEntry != null &&
                        !currentDestination.inHierarchy(typeRoute<ScreenRoute.BookList>())

            if (canNavigateBack) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        title = { Text(stringResource(titleRes)) },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

private inline fun <reified T : Any> typeRoute(): String =
    requireNotNull(T::class.qualifiedName) { "Route null for ${T::class}" }

private fun NavDestination?.inHierarchy(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true

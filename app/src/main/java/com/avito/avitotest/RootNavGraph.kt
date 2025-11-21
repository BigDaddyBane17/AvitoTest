package com.avito.avitotest

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import com.avito.avitotest.di.AppComponent
import com.avito.auth.navigation.AuthRoute
import com.avito.auth.navigation.authScreen
import com.avito.bookreader.navigation.BookReaderRoute
import com.avito.bookreader.navigation.bookReaderScreen
import com.avito.bookslist.navigation.BooksListRoute
import com.avito.bookslist.navigation.booksListScreen
import com.avito.bookupload.navigation.BookUploadRoute
import com.avito.bookupload.navigation.bookUploadScreen
import com.avito.navigation.ScreenRoute
import com.avito.profile.navigation.ProfileRoute
import com.avito.profile.navigation.profileScreen

@Composable
fun RootNavGraph(
    navController: NavHostController,
    startDestination: ScreenRoute,
    appComponent: AppComponent,
    googleWebClientId: String,
    modifier: Modifier = Modifier,
    onSettingsClickChanged: ((() -> Unit)?) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        addAuthGraph(navController, appComponent, googleWebClientId)
        addBookListGraph(navController, appComponent, onSettingsClickChanged)
        addUploadBookGraph(navController, appComponent)
        addProfileGraph(navController, appComponent)
    }
}


private fun NavGraphBuilder.addAuthGraph(
    navController: NavHostController,
    appComponent: AppComponent,
    googleWebClientId: String,
) {
    navigation<ScreenRoute.Auth>(
        startDestination = AuthRoute,
    ) {
        authScreen(
            authComponentFactory = appComponent.authComponentFactory(),
            webClientId = googleWebClientId,
            onAuthSuccess = {
                navController.navigate(ScreenRoute.BookList) {
                    popUpTo<ScreenRoute.Auth> {
                        inclusive = true
                    }
                }
            }
        )
    }
}


private fun NavGraphBuilder.addBookListGraph(
    navController: NavHostController,
    appComponent: AppComponent,
    onSettingsClickChanged: ((() -> Unit)?) -> Unit
) {
    navigation<ScreenRoute.BookList>(
        startDestination = BooksListRoute
    ) {
        booksListScreen(
            booksListComponentFactory = appComponent.booksListComponentFactory(),
            onBookClick = { bookId ->
                navController.navigate(BookReaderRoute(bookId))
            }
        )
        bookReaderScreen(
            bookReaderComponentFactory = appComponent.bookReaderComponentFactory(),
            onSettingsClickChanged = onSettingsClickChanged
        )
    }
}


private fun NavGraphBuilder.addUploadBookGraph(
    navController: NavHostController,
    appComponent: AppComponent,
) {
    navigation<ScreenRoute.UploadBook>(
        startDestination = BookUploadRoute
    ) {
        bookUploadScreen(
            bookUploadComponentFactory = appComponent.bookUploadComponentFactory(),
            onUploadCompleted = {
                navController.navigate(ScreenRoute.BookList) {
                    popUpTo<ScreenRoute.BookList> { inclusive = false }
                }
            }
        )
    }
}


private fun NavGraphBuilder.addProfileGraph(
    navController: NavHostController,
    appComponent: AppComponent,
) {
    navigation<ScreenRoute.Profile>(
        startDestination = ProfileRoute
    ) {
        profileScreen(
            profileComponentFactory = appComponent.profileComponentFactory(),
            onLogout = {
                navController.navigate(ScreenRoute.Auth) {
                    popUpTo<ScreenRoute.Auth> {
                        inclusive = true
                    }
                }
            }
        )
    }
}

package com.avito.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenRoute {

    @Serializable
    data object Auth : ScreenRoute

    @Serializable
    data object BookList : ScreenRoute

    @Serializable
    data object UploadBook : ScreenRoute

    @Serializable
    data object Profile : ScreenRoute
}

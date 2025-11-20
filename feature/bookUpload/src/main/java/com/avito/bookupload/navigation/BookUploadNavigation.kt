package com.avito.bookupload.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.bookupload.presentation.BookUploadScreen
import kotlinx.serialization.Serializable

@Serializable
data object BookUploadRoute

fun NavGraphBuilder.bookUploadScreen(
    onUploadCompleted: () -> Unit = {}
) {
    composable<BookUploadRoute> {
        BookUploadScreen(onUploadCompleted = onUploadCompleted)
    }
}


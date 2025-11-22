package com.avito.bookslist.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.avito.feature.bookslist.R

@Composable
fun BooksListEmptyState(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    val animationRes = if (isSearching) {
        R.raw.books_search_empty
    } else {
        R.raw.books_empty
    }
    val messageRes = if (isSearching) {
        R.string.books_list_empty_search
    } else {
        R.string.books_list_empty
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes)
    )
    val progress by animateLottieCompositionAsState(composition)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(220.dp)
        )
        Text(
            text = stringResource(id = messageRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


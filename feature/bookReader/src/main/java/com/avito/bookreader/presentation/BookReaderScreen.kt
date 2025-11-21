package com.avito.bookreader.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avito.bookreader.di.BookReaderComponent
import com.avito.bookreader.domain.FontSize
import com.avito.bookreader.domain.LineSpacing
import com.avito.bookreader.domain.ReadingTheme

@Composable
fun BookReaderScreen(
    bookReaderComponentFactory: BookReaderComponent.Factory,
    bookId: String,
    onSettingsClickChanged: ((() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val component = remember(bookReaderComponentFactory, bookId) {
        bookReaderComponentFactory.create(bookId)
    }
    val viewModelFactory = remember(component) { component.viewModelFactory() }
    val viewModel: BookReaderViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Set settings click callback
    LaunchedEffect(Unit) {
        onSettingsClickChanged {
            viewModel.onIntent(BookReaderIntent.ToggleSettings)
        }
    }

    // Clear callback on dispose
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            onSettingsClickChanged(null)
        }
    }

    when (val state = uiState) {
        BookReaderUiState.Loading -> LoadingState(modifier = modifier)
        is BookReaderUiState.Error -> ErrorState(
            message = state.message,
            bookId = bookId,
            onRetry = { viewModel.onIntent(BookReaderIntent.Retry) },
            onDelete = { viewModel.onIntent(BookReaderIntent.DeleteBook) },
            modifier = modifier
        )
        is BookReaderUiState.Content -> ContentState(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentState(
    state: BookReaderUiState.Content,
    onIntent: (BookReaderIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val backgroundColor = when (state.theme) {
        ReadingTheme.LIGHT -> Color.White
        ReadingTheme.DARK -> Color(0xFF1C1C1E)
        ReadingTheme.SEPIA -> Color(0xFFF4F1E8)
    }
    val textColor = when (state.theme) {
        ReadingTheme.LIGHT -> Color.Black
        ReadingTheme.DARK -> Color(0xFFE5E5E7)
        ReadingTheme.SEPIA -> Color(0xFF5F4B32)
    }

    LaunchedEffect(state.scrollPosition) {
        if (state.scrollPosition > 0 && scrollState.value == 0) {
            scrollState.scrollTo(state.scrollPosition)
        }
    }

    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        onIntent(BookReaderIntent.ScrollPositionChanged(scrollState.value, scrollState.maxValue))
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = backgroundColor,
            bottomBar = {
            Column {
                LinearProgressIndicator(
                    progress = state.readingProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Прочитано: ${state.readingProgress.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
        }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = state.book.text,
                        fontSize = (16 * state.fontSize.scale).sp,
                        lineHeight = (16 * state.fontSize.scale * state.lineSpacing.value).sp,
                        color = textColor,
                        textAlign = TextAlign.Justify
                    )
                }

                AnimatedVisibility(
                    visible = state.isSettingsVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    SettingsPanel(
                        fontSize = state.fontSize,
                        lineSpacing = state.lineSpacing,
                        theme = state.theme,
                        onFontSizeChanged = { onIntent(BookReaderIntent.FontSizeChanged(it)) },
                        onLineSpacingChanged = { onIntent(BookReaderIntent.LineSpacingChanged(it)) },
                        onThemeChanged = { onIntent(BookReaderIntent.ThemeChanged(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    fontSize: FontSize,
    lineSpacing: LineSpacing,
    theme: ReadingTheme,
    onFontSizeChanged: (FontSize) -> Unit,
    onLineSpacingChanged: (LineSpacing) -> Unit,
    onThemeChanged: (ReadingTheme) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Настройки отображения",
                style = MaterialTheme.typography.titleMedium
            )

            SettingSection(
                title = "Размер шрифта",
                items = FontSize.entries,
                selectedItem = fontSize,
                onItemSelected = onFontSizeChanged,
                labelProvider = { it.label }
            )

            SettingSection(
                title = "Межстрочный интервал",
                items = LineSpacing.entries,
                selectedItem = lineSpacing,
                onItemSelected = onLineSpacingChanged,
                labelProvider = { it.label }
            )

            SettingSection(
                title = "Тема",
                items = ReadingTheme.entries,
                selectedItem = theme,
                onItemSelected = onThemeChanged,
                labelProvider = { it.label }
            )
        }
    }
}

@Composable
private fun <T> SettingSection(
    title: String,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                FilterChip(
                    selected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                    label = { Text(labelProvider(item)) }
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    bookId: String,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onRetry) {
                Text("Повторить")
            }
            Button(
                onClick = onDelete,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Удалить")
            }
        }
    }
}

private val FontSize.label: String
    get() = when (this) {
        FontSize.SMALL -> "Мал."
        FontSize.MEDIUM -> "Сред."
        FontSize.LARGE -> "Бол."
        FontSize.EXTRA_LARGE -> "Оч.бол."
    }

private val LineSpacing.label: String
    get() = when (this) {
        LineSpacing.COMPACT -> "Комп."
        LineSpacing.NORMAL -> "Норм."
        LineSpacing.RELAXED -> "Широк."
    }

private val ReadingTheme.label: String
    get() = when (this) {
        ReadingTheme.LIGHT -> "Светлая"
        ReadingTheme.DARK -> "Тёмная"
        ReadingTheme.SEPIA -> "Сепия"
    }

package com.avito.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

data class TopBarConfig(
    val title: String,
    val showBackButton: Boolean = false,
    val onBackClick: (() -> Unit)? = null,
    val actions: @Composable RowScope.() -> Unit = {},
    val search: TopBarSearchConfig? = null
) {
}

data class TopBarSearchConfig(
    val value: String,
    val onValueChange: (String) -> Unit,
    val placeholder: String
)


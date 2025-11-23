package com.avito.profile.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avito.profile.di.ProfileComponent
import com.avito.profile.presentation.composable.ProfileContent
import com.avito.profile.presentation.composable.ProfileLoading
import com.avito.navigation.TopBarConfig

@Composable
fun ProfileScreen(
    profileComponentFactory: ProfileComponent.Factory,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    val profileComponent = remember(profileComponentFactory) { profileComponentFactory.create() }
    val viewModelFactory = remember(profileComponent) { profileComponent.viewModelFactory() }
    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onIntent(ProfileIntent.PhotoSelected(it)) }
    }

    val contentState = uiState as? ProfileUiState.Content
    
    LaunchedEffect(contentState?.message) {
        val message = contentState?.message
        if (message == ProfileViewModel.LOGOUT_MESSAGE) {
            viewModel.onIntent(ProfileIntent.DismissMessage)
            onLogout()
        } else if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(ProfileIntent.DismissMessage)
        }
    }
    
    LaunchedEffect(contentState?.errorMessage) {
        contentState?.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(ProfileIntent.DismissMessage)
        }
    }

    LaunchedEffect(Unit) {
        onTopBarConfigChange(TopBarConfig(title = "Профиль"))
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            ProfileUiState.Loading -> ProfileLoading()
            is ProfileUiState.Content -> ProfileContent(
                state = state,
                onIntent = viewModel::onIntent,
                onPickPhoto = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


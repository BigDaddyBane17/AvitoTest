package com.avito.profile.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.avito.feature.profile.R
import com.avito.profile.di.ProfileComponent

@Composable
fun ProfileScreen(
    profileComponentFactory: ProfileComponent.Factory,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profileComponent = remember(profileComponentFactory) { profileComponentFactory.create() }
    val viewModelFactory = remember(profileComponent) { profileComponent.viewModelFactory() }
    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onIntent(ProfileIntent.PhotoSelected(it)) }
    }

    val contentState = uiState as? ProfileUiState.Content
    LaunchedEffect(contentState?.message) {
        if (contentState?.message == ProfileViewModel.LOGOUT_MESSAGE) {
            viewModel.onIntent(ProfileIntent.DismissMessage)
            onLogout()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            ProfileUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Content -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileAvatar(
                        photoUrl = state.photoUrl,
                        onClick = { galleryLauncher.launch("image/*") }
                    )

                    if (state.isEditing) {
                        TextField(
                            value = state.displayName,
                            onValueChange = { viewModel.onIntent(ProfileIntent.DisplayNameChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(id = R.string.profile_edit)) },
                            singleLine = true,
                            enabled = !state.isSaving,
                            shape = MaterialTheme.shapes.large,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            )
                        )
                    } else {
                        Text(
                            text = state.displayName.ifEmpty { stringResource(id = R.string.profile_name_placeholder) },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(text = state.email, style = MaterialTheme.typography.bodyMedium)
                    if (state.phone.isNotBlank()) {
                        Text(text = state.phone, style = MaterialTheme.typography.bodyMedium)
                    }

                    state.errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                    if (state.message != null && state.message != ProfileViewModel.LOGOUT_MESSAGE) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.primary)
                    }

                    if (state.isSaving) {
                        CircularProgressIndicator()
                    }

                    OutlinedButton(
                        onClick = { viewModel.onIntent(ProfileIntent.ToggleEditMode) },
                        enabled = !state.isSaving
                    ) {
                        Text(
                            text = if (state.isEditing) {
                                stringResource(id = R.string.profile_cancel)
                            } else {
                                stringResource(id = R.string.profile_edit)
                            }
                        )
                    }
                    if (state.isEditing) {
                        OutlinedButton(
                            onClick = { viewModel.onIntent(ProfileIntent.SaveChanges) },
                            enabled = !state.isSaving
                        ) {
                            Text(stringResource(id = R.string.profile_save))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.onIntent(ProfileIntent.Logout) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    ) {
                        Text(stringResource(id = R.string.profile_logout))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    photoUrl: Uri?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick,
        shape = CircleShape,
        tonalElevation = 4.dp,
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = stringResource(id = R.string.profile_photo_placeholder),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = stringResource(id = R.string.profile_photo_stub),
                    modifier = Modifier.size(64.dp),
                )
            }
        }
    }
}

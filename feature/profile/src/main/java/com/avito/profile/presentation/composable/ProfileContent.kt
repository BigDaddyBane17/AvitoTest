package com.avito.profile.presentation.composable

import android.net.Uri
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.avito.feature.profile.R
import com.avito.profile.presentation.ProfileIntent
import com.avito.profile.presentation.ProfileUiState
import com.avito.profile.presentation.ProfileViewModel
import com.avito.ui.components.ScreenDefaults
import com.avito.ui.components.UiLoadingState
import com.avito.ui.components.UiLoadingState

@Composable
fun ProfileLoading(modifier: Modifier = Modifier) {
    UiLoadingState(modifier = modifier)
}

@Composable
fun ProfileContent(
    state: ProfileUiState.Content,
    onIntent: (ProfileIntent) -> Unit,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ScreenDefaults.ContentPadding)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileAvatar(
            photoUrl = state.photoUrl,
            onClick = onPickPhoto
        )

        if (state.isEditing) {
            TextField(
                value = state.displayName,
                onValueChange = { onIntent(ProfileIntent.DisplayNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.profile_edit)) },
                singleLine = true,
                enabled = !state.isSaving,
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
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

        if (state.isSaving) {
            CircularProgressIndicator()
        }

        OutlinedButton(
            onClick = { onIntent(ProfileIntent.ToggleEditMode) },
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
                onClick = { onIntent(ProfileIntent.SaveChanges) },
                enabled = !state.isSaving
            ) {
                Text(stringResource(id = R.string.profile_save))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onIntent(ProfileIntent.Logout) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            Text(stringResource(id = R.string.profile_logout))
        }
    }
}

@Composable
fun ProfileAvatar(
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
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
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


package com.samos.osmand.presentation.screen.download

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samos.osmand.domain.model.DownloadStatus
import com.samos.osmand.presentation.base.ToastManager
import com.samos.osmand.presentation.base.component.TopAppBar
import com.samos.osmand.presentation.base.noRippleClickable
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadEffect
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadIntent
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadState
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadViewModel
import com.samos.osmand.presentation.screen.download.viewmodel.MapDownloadItemModel
import com.samos.osmand.presentation.screen.download.viewmodel.ToastType
import com.samos.osmand.presentation.theme.MapDownloadedColor
import com.samos.osmand.presentation.theme.MapDownloadingProgressColor
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import osmand.shared.generated.resources.Res
import osmand.shared.generated.resources.download_complete
import osmand.shared.generated.resources.download_maps
import osmand.shared.generated.resources.ic_cancel
import osmand.shared.generated.resources.ic_delete
import osmand.shared.generated.resources.ic_download
import osmand.shared.generated.resources.ic_map
import osmand.shared.generated.resources.no_internet_connection
import osmand.shared.generated.resources.successfully_deleted

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastManager = koinInject<ToastManager>()
    var toastResId by remember { mutableStateOf<StringResource?>(null) }

    LaunchedEffect(Unit) {
        for (effect in viewModel.effect) {
            when (effect) {
                is DownloadEffect.ShowToast -> {
                    toastResId = when (effect.type) {
                        ToastType.OnMapDownloaded -> Res.string.download_complete
                        ToastType.OnMapDeleted -> Res.string.successfully_deleted
                        ToastType.NoInternetConnection -> Res.string.no_internet_connection
                    }
                }
            }
        }
    }
    toastResId?.let { resId ->
        val message = stringResource(resId)
        toastManager.showToast(message)
        toastResId = null
    }

    DownloadScreenContent(
        state = state,
        onNavigationIconClick = {
            viewModel.handleIntent(DownloadIntent.NavigateBack)
        },
        onMapDownloadItemClick = { itemId ->
            viewModel.handleIntent(DownloadIntent.OnMapDownloadItemClick(itemId))
        },
        onDownloadMapClick = { itemId ->
            viewModel.handleIntent(DownloadIntent.OnDownloadMapClick(itemId))
        },
        onDeleteMapClick = { itemId ->
            viewModel.handleIntent(DownloadIntent.OnDeleteMapClick(itemId))
        },
        onCancelMapDeletion = {
            viewModel.handleIntent(DownloadIntent.OnCancelMapDeletion)
        },
        onConfirmMapDeletion = { itemId ->
            viewModel.handleIntent(DownloadIntent.OnConfirmDeletiuon(itemId))
        },
        onCancelDownloadClick = { itemId ->
            viewModel.handleIntent(DownloadIntent.OnCancelDownloadClick(itemId))
        },
    )
}

@Composable
fun DownloadScreenContent(
    state: DownloadState,
    onNavigationIconClick: () -> Unit = {},
    onMapDownloadItemClick: (String) -> Unit = {},
    onDownloadMapClick: (String) -> Unit = {},
    onDeleteMapClick: (String) -> Unit = {},
    onCancelMapDeletion: () -> Unit = {},
    onConfirmMapDeletion: (String) -> Unit = {},
    onCancelDownloadClick: (String) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = buildAnnotatedString {
                    append(state.title ?: stringResource(Res.string.download_maps))
                },
                hasNavigationIcon = state.title != null,
                onNavigationIconClick = onNavigationIconClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
        ) {
            state.usableMemory?.let {
                DeviceMemoryBar(
                    usableMemoryAmount = state.usableMemory,
                    usedMemoryProgress = state.usedMemoryProgress,
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomSpacer()
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.items,
                    key = { item -> item.id }
                ) { item ->
                    DownloadItemRow(
                        item = item,
                        onMapDownloadItemClick = onMapDownloadItemClick,
                        onDownloadMapClick = onDownloadMapClick,
                        onDeleteMapClick = onDeleteMapClick,
                        onCancelDownloadClick = onCancelDownloadClick,
                    )
                }
            }

            CustomSpacer(showBottomShadow = false)
        }
    }

    if (state.mapIdToDelete != null) {
        ConfirmMapDeletionAlertDialog(
            mapIdToDeleteId = state.mapIdToDelete,
            mapDisplayName = state.items
                .firstOrNull { it.id == state.mapIdToDelete }?.displayName ?: state.mapIdToDelete,
            onDismissRequest = onCancelMapDeletion,
            onConfirmDeletion = onConfirmMapDeletion,
        )
    }
}

@Composable
private fun DeviceMemoryBar(
    usableMemoryAmount: String?,
    usedMemoryProgress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(vertical = 12.dp)
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Device memory",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.Ellipsis,
                minLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
            usableMemoryAmount?.let {
                Text(
                    text = "Free $usableMemoryAmount",
                    color = MaterialTheme.colorScheme.onBackground,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        LinearProgressIndicator(
            progress = { usedMemoryProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(16.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.background,
            strokeCap = StrokeCap.Square
        )

    }
}

@Composable
fun CustomSpacer(
    showTopShadow: Boolean = true,
    showBottomShadow: Boolean = true,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(24.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (showTopShadow) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        1.0f to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.01f),
                    )
                )
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
            )
        }
        if (showBottomShadow) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.01f),
                        1.0f to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                    )
                )
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun DownloadItemRow(
    item: MapDownloadItemModel,
    onMapDownloadItemClick: (String) -> Unit = {},
    onDownloadMapClick: (String) -> Unit = {},
    onDeleteMapClick: (String) -> Unit = {},
    onCancelDownloadClick: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
            .noRippleClickable {
                if (item.isContainer) onMapDownloadItemClick.invoke(item.id)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val leftIconTint = if (item.status is DownloadStatus.Downloaded) MapDownloadedColor
        else MaterialTheme.colorScheme.onSurface
        Icon(
            painter = painterResource(Res.drawable.ic_map),
            contentDescription = "Map Icon",
            modifier = Modifier.padding(start = 16.dp),
            tint = leftIconTint
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = item.displayName,
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (item.status is DownloadStatus.Downloading) {
                LinearProgressIndicator(
                    progress = { item.status.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(3.dp),
                    color = MapDownloadingProgressColor,
                    trackColor = MaterialTheme.colorScheme.background,
                )
            }

            if (item.status is DownloadStatus.InQueue) {
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(3.dp),
                    color = MapDownloadingProgressColor,
                    trackColor = MaterialTheme.colorScheme.background,
                )
            }
        }

        if (!item.isContainer) {
            when (item.status) {
                is DownloadStatus.Downloaded -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = "Delete Map",
                        modifier = Modifier
                            .noRippleClickable { onDeleteMapClick.invoke(item.id) }
                            .size(24.dp)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                is DownloadStatus.Downloading, DownloadStatus.InQueue -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_cancel),
                        contentDescription = "Cancel Download",
                        modifier = Modifier
                            .noRippleClickable { onCancelDownloadClick.invoke(item.id) },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                else -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_download),
                        contentDescription = "Start Download",
                        modifier = Modifier
                            .noRippleClickable { onDownloadMapClick.invoke(item.id) },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmMapDeletionAlertDialog(
    mapIdToDeleteId: String,
    mapDisplayName: String,
    onDismissRequest: () -> Unit,
    onConfirmDeletion: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest, //{ onDismissRequest.invoke() },
        title = {
            Text(
                text = "Delete Map",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete\nthe map for $mapDisplayName?\n\nThis action cannot be undone.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDeletion(mapIdToDeleteId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8800))
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    )
}

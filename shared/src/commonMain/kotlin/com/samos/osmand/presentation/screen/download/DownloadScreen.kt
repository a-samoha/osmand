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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.samos.osmand.presentation.base.component.TopAppBar
import com.samos.osmand.presentation.base.noRippleClickable
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadIntent
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadState
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadViewModel
import com.samos.osmand.presentation.screen.download.viewmodel.MapDownloadItemModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import osmand.shared.generated.resources.Res
import osmand.shared.generated.resources.download_maps
import osmand.shared.generated.resources.ic_cancel
import osmand.shared.generated.resources.ic_delete
import osmand.shared.generated.resources.ic_download
import osmand.shared.generated.resources.ic_map

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        Icon(
            painter = painterResource(Res.drawable.ic_map),
            contentDescription = "Map Icon",
            modifier = Modifier.padding(start = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface
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
                    color = Color(0xFF2175F3),
                    trackColor = Color(0xFFE5E5E5)
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

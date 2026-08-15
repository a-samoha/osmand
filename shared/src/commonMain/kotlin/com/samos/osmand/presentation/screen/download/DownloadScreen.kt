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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samos.osmand.presentation.base.component.TopAppBar
import com.samos.osmand.presentation.base.noRippleClickable
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadIntent
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadState
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadViewModel
import com.samos.osmand.presentation.screen.download.viewmodel.MapDownloadItemModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import osmand.shared.generated.resources.Res
import osmand.shared.generated.resources.ic_download
import osmand.shared.generated.resources.ic_map

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DownloadScreenContent(
        state = state,
        onNavigationIconClick = { viewModel.handleIntent(DownloadIntent.NavigateBack) },
        onDownloadMapClick = { fileName ->
            viewModel.handleIntent(
                DownloadIntent.OnDownloadMapClick(
                    fileName
                )
            )
        },
        onDeleteMapClick = { fileName ->
            viewModel.handleIntent(
                DownloadIntent.OnDeleteMapClick(
                    fileName
                )
            )
        },
    )
}

@Composable
fun DownloadScreenContent(
    state: DownloadState,
    onNavigationIconClick: () -> Unit = {},
    onDownloadMapClick: (String) -> Unit = {},
    onDeleteMapClick: (String) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = buildAnnotatedString { append("Download Maps") },
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
            DeviceMemoryBar(
                usableMemoryAmount = state.usableMemory,
                usedMemoryProgress = state.usedMemoryProgress,
                modifier = Modifier.fillMaxWidth(),
            )

            CustomSpacer()

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                stickyHeader {
                    Text(
                        text = "Europe",
                        modifier = Modifier
                            .padding(start = 20.dp, top = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        overflow = TextOverflow.Ellipsis,
                        minLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                items(
                    items = state.items,
                    key = { item -> item.id }
                ) { item ->
                    DownloadItemRow(
                        item = item,
                        onDownloadMapClick = onDownloadMapClick,
                        onDeleteMapClick = onDeleteMapClick,
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
    onDownloadMapClick: (String) -> Unit = {},
    onDeleteMapClick: (String) -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
            .noRippleClickable { onDeleteMapClick.invoke(item.id) }, // "Denmark_capital-region_europe_2.obf.zip"
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_map),
            contentDescription = "Map",
            modifier = Modifier.padding(start = 4.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = item.fileName,
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .noRippleClickable { onDownloadMapClick.invoke(item.id) }, // "Denmark_capital-region_europe_2.obf.zip"
            painter = painterResource(Res.drawable.ic_download),
            contentDescription = "Download",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

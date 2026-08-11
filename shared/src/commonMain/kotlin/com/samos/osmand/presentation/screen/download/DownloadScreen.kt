package com.samos.osmand.presentation.screen.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samos.osmand.presentation.base.component.TopAppBar
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadIntent
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadState
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DownloadScreenContent(
        state = state,
        onNavigationIconClick = { viewModel.handleIntent(DownloadIntent.NavigateBack) }
    )
}

@Composable
fun DownloadScreenContent(
    state: DownloadState,
    onNavigationIconClick: () -> Unit = {},
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

    }
}

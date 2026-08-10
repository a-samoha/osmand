package com.samos.osmand

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.samos.osmand.presentation.navigation.MainNavHost
import com.samos.osmand.presentation.theme.ArthaAppTheme

@Composable
@Preview
fun SharedCompose() {
    ArthaAppTheme {
        MainNavHost()
    }
}

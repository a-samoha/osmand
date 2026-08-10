package com.osmand.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.osmand.myapplication.presentation.navigation.MainNavHost
import com.osmand.myapplication.presentation.theme.ArthaAppTheme

@Composable
@Preview
fun SharedCompose() {
    ArthaAppTheme {
        MainNavHost()
    }
}

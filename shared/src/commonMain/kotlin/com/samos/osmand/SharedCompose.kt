package com.samos.osmand

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.samos.osmand.presentation.navigation.MainNavHost
import com.samos.osmand.presentation.theme.OsmandAppTheme

@Composable
@Preview
fun SharedCompose() {
    OsmandAppTheme {
        MainNavHost()
    }
}

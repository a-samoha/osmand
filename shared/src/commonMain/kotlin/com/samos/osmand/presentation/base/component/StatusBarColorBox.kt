package com.samos.osmand.presentation.base.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun StatusBarColorBox(
    containerColor: Color,
    darkIcons: Boolean,
    modifier: Modifier = Modifier
) {
    DynamicStatusBarIcons(darkIcons = darkIcons)

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .windowInsetsTopHeight(WindowInsets.statusBars)
    )
}

@Composable
expect fun DynamicStatusBarIcons(darkIcons: Boolean)
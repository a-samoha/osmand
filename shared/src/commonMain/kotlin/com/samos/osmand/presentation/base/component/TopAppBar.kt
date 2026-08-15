package com.samos.osmand.presentation.base.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samos.osmand.presentation.base.TestTags
import com.samos.osmand.presentation.base.conditional
import com.samos.osmand.presentation.base.noRippleClickable
import org.jetbrains.compose.resources.painterResource
import osmand.shared.generated.resources.Res
import osmand.shared.generated.resources.ic_back_arrow

@Composable
fun TopAppBar(
    title: AnnotatedString,
    modifier: Modifier = Modifier,
    tag: String = TestTags.TOP_BAR,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    titleContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    hasShadow: Boolean = true,
    hasNavigationIcon: Boolean = false,
    onNavigationIconClick: () -> Unit,
) {
    TopAppBarContainer(
        topBarContent = {
            StatusBarColorBox(
                containerColor = MaterialTheme.colorScheme.inversePrimary,
                darkIcons = false,
            )
            CenterAlignedTopAppBar(
                modifier = modifier
                    .statusBarsPadding()
                    .testTag(tag),
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = titleContentColor,
                ),
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (hasNavigationIcon) {
                        Icon(
                            modifier = Modifier
                                .noRippleClickable { onNavigationIconClick() }
                                .padding(8.dp)
                                .testTag(tag),
                            painter = painterResource(Res.drawable.ic_back_arrow),
                            contentDescription = "Navigate Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
            )
        },
        hasShadow = hasShadow,
    )
}

@Composable
private fun TopAppBarContainer(
    topBarContent: @Composable () -> Unit,
    hasShadow: Boolean,
    isTransparentBackground: Boolean = false,
    shouldIncludeShadowPadding: Boolean = false,
) {
    val topBarHeight = remember { mutableIntStateOf(0) }
    val colorBackground = MaterialTheme.colorScheme.background

    val surfaceModifier = Modifier
        .fillMaxWidth()
        .conditional(hasShadow) {
            drawBehind {
                val shadowHeight = 14.dp.toPx()
                val shadowAlpha = 0.10f

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xD9000000).copy(alpha = shadowAlpha),
                            Color.Transparent
                        ),
                        startY = size.height,
                        endY = size.height + shadowHeight
                    ),
                    topLeft = Offset(0f, size.height),
                    size = Size(size.width, shadowHeight)
                )
            }
        }
        .conditional(!isTransparentBackground) {
            background(colorBackground)
        }
        .conditional(hasShadow && shouldIncludeShadowPadding) {
            padding(bottom = 8.dp)
        }
        .onGloballyPositioned {
            if (it.isAttached) {
                topBarHeight.intValue = (it.positionInParent().y + it.size.height).toInt()
            }
        }

    val backgroundColor = if (isTransparentBackground) {
        Color.Transparent
    } else {
        colorBackground
    }

    Surface(
        modifier = surfaceModifier,
        color = backgroundColor,
        content = topBarContent
    )
}

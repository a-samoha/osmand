package com.samos.osmand.presentation.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

fun Modifier.noRippleClickable(
    debounceTime: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    val timeSource = remember { TimeSource.Monotonic }
    var lastClickMark by remember {
        mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null)
    }
    val interactionSource = remember { MutableInteractionSource() }

    this.clickable(
        indication = null,
        interactionSource = interactionSource
    ) {
        val currentMark = timeSource.markNow()
        val previousMark = lastClickMark

        if (previousMark == null || (currentMark - previousMark) >= debounceTime.milliseconds) {
            lastClickMark = currentMark
            onClick()
        }
    }
}

/**
 * Conditionally applies a [Modifier] based on the provided [condition].
 *
 * If [condition] is true, the [modifier] lambda will be invoked and its result
 * will be applied to the current [Modifier]. If [condition] is false, the original
 * [Modifier] will be returned unchanged.
 *
 * @param condition The boolean condition that determines whether to apply the modifier.
 * @param modifier A lambda that takes a [Modifier] and returns a modified [Modifier] to apply when the condition is true.
 * @return The resulting [Modifier] after conditionally applying the provided modifier.
 */
fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

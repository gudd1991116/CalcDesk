package com.qiyuan.calcdesk.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val DeleteBtnWidth = 88.dp
private val DeleteRed = Color(0xFFE53935)

/**
 * iOS-style swipe: drag left to reveal a red Delete affordance whose width equals
 * how far the row has moved; drag right to animate the row back over the button.
 */
@Composable
fun SwipeRevealDelete(
    deleteLabel: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val revealPx = with(density) { DeleteBtnWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun settle() {
        scope.launch {
            val target = if (offsetX.value <= -revealPx / 2f) -revealPx else 0f
            offsetX.animateTo(target, animationSpec = tween(220))
        }
    }

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        content = {
            // 0: delete background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(DeleteRed),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .width(DeleteBtnWidth)
                        .fillMaxHeight()
                        .clickable {
                            scope.launch {
                                offsetX.animateTo(0f, tween(160))
                                onDelete()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = deleteLabel,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            // 1: foreground row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(revealPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = { settle() },
                            onDragCancel = { settle() },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount).coerceIn(-revealPx, 0f),
                                    )
                                }
                            },
                        )
                    },
                content = content,
            )
        },
    ) { measurables, constraints ->
        val foreground = measurables[1].measure(constraints)
        val background = measurables[0].measure(
            constraints.copy(
                minHeight = foreground.height,
                maxHeight = foreground.height,
            ),
        )
        layout(foreground.width, foreground.height) {
            background.placeRelative(0, 0)
            foreground.placeRelative(0, 0)
        }
    }
}

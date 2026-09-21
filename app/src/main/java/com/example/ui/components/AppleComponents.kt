package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleGreenDark
import com.example.ui.theme.AppleGreenLight
import com.example.ui.theme.AppleSwitchTrackOffDark
import com.example.ui.theme.AppleSwitchTrackOffLight
import com.example.ui.theme.GradientBlueDark
import com.example.ui.theme.GradientBlueLight
import com.example.ui.theme.GradientCyanLight
import com.example.ui.theme.GradientGreenDark
import com.example.ui.theme.GradientGreenLight
import com.example.ui.theme.GradientTealLight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic Apple iOS Toggle Switch
 * 51dp x 31dp capsule with 27dp circular sliding white thumb with soft elevation shadow.
 */
@Composable
fun AppleSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = if (MaterialTheme.colorScheme.background == Color.Black) AppleGreenDark else AppleGreenLight,
    inactiveColor: Color = if (MaterialTheme.colorScheme.background == Color.Black) AppleSwitchTrackOffDark else AppleSwitchTrackOffLight,
    testTag: String = "apple_switch"
) {
    val interactionSource = remember { MutableInteractionSource() }

    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "switchTrackColor"
    )

    // Thumb offset: 2.dp when unchecked, 22.dp when checked
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switchThumbOffset"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .width(51.dp)
            .height(31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange?.invoke(!checked)
            }
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .shadow(elevation = 2.5.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Gentle moving gradient background modifier (Green to Blue with gentle, soothing movement).
 */
@Composable
fun Modifier.gentleMovingGradient(
    isDark: Boolean = false,
    alpha: Float = 0.22f
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "gentleGradient")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientProgress"
    )

    val color1 = if (isDark) GradientGreenDark else GradientGreenLight
    val color2 = if (isDark) GradientTealLight else GradientTealLight
    val color3 = if (isDark) GradientBlueDark else GradientBlueLight
    val color4 = if (isDark) GradientCyanLight else GradientCyanLight

    return this.drawBehind {
        val angle = progress * 2f * Math.PI.toFloat()
        val startX = size.width * (0.3f + 0.2f * cos(angle))
        val startY = size.height * (0.2f + 0.2f * sin(angle))
        val endX = size.width * (0.8f - 0.2f * cos(angle))
        val endY = size.height * (0.8f - 0.2f * sin(angle))

        val brush = Brush.linearGradient(
            colors = listOf(
                color1.copy(alpha = alpha),
                color2.copy(alpha = alpha * 0.9f),
                color3.copy(alpha = alpha),
                color4.copy(alpha = alpha * 0.85f)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY)
        )
        drawRect(brush = brush)
    }
}

/**
 * Authentic Apple Pill Button with tactile feedback
 */
enum class AppleButtonStyle {
    PRIMARY_FILLED,
    SECONDARY_TINTED,
    GENTLE_GRADIENT,
    DESTRUCTIVE,
    OUTLINED
}

@Composable
fun AppleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    style: AppleButtonStyle = AppleButtonStyle.PRIMARY_FILLED,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 11.dp),
    testTag: String = "apple_button",
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    val isDark = MaterialTheme.colorScheme.background == Color.Black

    val (bgColor, contentColor, borderStroke) = when (style) {
        AppleButtonStyle.PRIMARY_FILLED -> Triple(
            MaterialTheme.colorScheme.primary,
            Color.White,
            null
        )
        AppleButtonStyle.SECONDARY_TINTED -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.18f else 0.12f),
            MaterialTheme.colorScheme.primary,
            null
        )
        AppleButtonStyle.GENTLE_GRADIENT -> Triple(
            Color.Transparent,
            Color.White,
            null
        )
        AppleButtonStyle.DESTRUCTIVE -> Triple(
            Color(0xFFFF3B30).copy(alpha = if (isDark) 0.18f else 0.12f),
            Color(0xFFFF3B30),
            null
        )
        AppleButtonStyle.OUTLINED -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurface,
            0.5.dp
        )
    }

    val gradientBrush = if (style == AppleButtonStyle.GENTLE_GRADIENT) {
        val infiniteTransition = rememberInfiniteTransition(label = "btnGrad")
        val shift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shift"
        )
        val c1 = if (isDark) GradientGreenDark else GradientGreenLight
        val c2 = if (isDark) GradientBlueDark else GradientBlueLight
        Brush.horizontalGradient(
            colors = listOf(c1, c2),
            startX = shift * 50f,
            endX = 400f + shift * 50f
        )
    } else null

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .then(
                if (gradientBrush != null) {
                    Modifier.background(gradientBrush)
                } else {
                    Modifier.background(bgColor)
                }
            )
            .then(
                if (borderStroke != null) {
                    Modifier.border(borderStroke, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onClick()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (content != null) {
                content()
            } else {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(17.dp)
                    )
                    if (!text.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(7.dp))
                    }
                }
                if (!text.isNullOrBlank()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Animated float helper
 */
@Composable
private fun animateFloatAsState(
    targetValue: Float,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float>,
    label: String
) = androidx.compose.animation.core.animateFloatAsState(targetValue, animationSpec, label = label)

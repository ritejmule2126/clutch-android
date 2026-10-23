package com.example.clutch_ai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clutch_ai.ui.theme.CardSurface
import com.example.clutch_ai.ui.theme.ClayBorder
import com.example.clutch_ai.ui.theme.ElectricCyan
import com.example.clutch_ai.ui.theme.ElectricCyanDark
import com.example.clutch_ai.ui.theme.MidnightChassis
import com.example.clutch_ai.ui.theme.MidnightChassisBorder
import com.example.clutch_ai.ui.theme.MidnightChassisCard
import com.example.clutch_ai.ui.theme.MidnightChassisSurface
import com.example.clutch_ai.ui.theme.TextMuted
import com.example.clutch_ai.ui.theme.TextPrimary

/**
 * Modern Minimalist Surface Modifier.
 * Pure, crisp, shadow-free layout with clean hairline borders and smooth corners.
 */
fun Modifier.claymorphic(
    color: Color = CardSurface,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 0.dp, // Intentionally ignored - shadowless design
    isPressed: Boolean = false,
    glowColor: Color? = null,
    borderWidth: Dp = 1.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val borderColor = glowColor ?: when (color) {
        MidnightChassis, MidnightChassisCard, MidnightChassisSurface -> MidnightChassisBorder
        else -> ClayBorder
    }

    return this
        .clip(shape)
        .background(color)
        .border(width = borderWidth, color = borderColor, shape = shape)
}

@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardSurface,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 0.dp,
    glowColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1.0f,
        label = "cardScale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val borderColor = glowColor ?: when (backgroundColor) {
        MidnightChassis, MidnightChassisCard, MidnightChassisSurface -> MidnightChassisBorder
        else -> ClayBorder
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MidnightChassis,
    contentColor: Color = Color.White,
    cornerRadius: Dp = 14.dp,
    glowColor: Color? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        label = "btnScale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val borderColor = glowColor ?: when (backgroundColor) {
        MidnightChassis, MidnightChassisCard, MidnightChassisSurface -> MidnightChassisBorder
        else -> ClayBorder
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun ClayPill(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    activeColor: Color = MidnightChassis,
    activeTextColor: Color = ElectricCyan,
    inactiveColor: Color = CardSurface,
    inactiveTextColor: Color = TextPrimary,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1.0f,
        label = "pillScale"
    )

    val bgColor = if (isSelected) activeColor else inactiveColor
    val textColor = if (isSelected) activeTextColor else inactiveTextColor
    val borderColor = if (isSelected) {
        activeTextColor.copy(alpha = 0.45f)
    } else {
        ClayBorder
    }

    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.invoke()
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ClayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    cornerRadius: Dp = 16.dp
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(CardSurface)
            .border(width = 1.dp, color = ClayBorder, shape = shape)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.invoke()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (leadingIcon != null) 10.dp else 4.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = TextPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(MidnightChassis),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            trailingIcon?.invoke()
        }
    }
}

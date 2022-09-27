package com.example.zygos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.material.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zygos.ui.theme.EczarFontFamily
import com.example.zygos.ui.theme.Roboto
import com.example.zygos.ui.theme.ZygosTheme


const val maxCharacters = 7

val style = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp,
    fontFamily = EczarFontFamily,
//    letterSpacing = 1.5.sp,
    lineHeight = 18.sp,
)

@Composable
fun TickerSelector(
    ticker: String,
    modifier: Modifier = Modifier,
    onTickerGo: (String) -> Unit = { },
) {
    var currentText by remember { mutableStateOf(ticker) }
    val focusManager = LocalFocusManager.current

    CustomTextField(
        value = currentText,
        textStyle = style,
        singleLine = true,
        colors = outlinedTextFieldColors(
            textColor = MaterialTheme.colors.onSurface,
        ),
        placeholder = {
            Text(
                text = "ticker",
                style = style.copy(fontFamily = Roboto, fontSize = 22.sp),
                modifier = Modifier.padding(start = 4.dp)
            )
        },
        onValueChange = { if (it.length <= maxCharacters) currentText = it },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrect = false,
            imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(
            onGo = {
                focusManager.clearFocus()
                onTickerGo(currentText)
            }
        ),
        modifier = modifier
    )
}

/**
 * Copied from OutlinedTextField, but removes the inner padding
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) {
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    @OptIn(ExperimentalMaterialApi::class)
    BasicTextField(
        value = value,
        modifier = if (label != null) {
            modifier
                // Merge semantics at the beginning of the modifier chain to ensure padding is
                // considered part of the text field.
                .semantics(mergeDescendants = true) {}
                //.padding(top = OutlinedTextFieldTopPadding) // Removed!
        } else {
            modifier
        }
//            .background(colors.backgroundColor(enabled).value, shape)
            .indicatorLine(enabled, isError, interactionSource, colors),
//            .defaultMinSize( // Removed!
//                minWidth = TextFieldDefaults.MinWidth,
//                minHeight = TextFieldDefaults.MinHeight
//            ),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError).value),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            // places leading icon, text field with label and placeholder, trailing icon
            TextFieldDefaults.TextFieldDecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp), // Edited!!!!!
            )
        },
    )
}


@Preview
@Composable
fun PreviewTickerSelector() {
    ZygosTheme {
        Surface {
            Column {
                TickerSelector(ticker = "MSFT")
                Spacer(Modifier.height(20.dp))
                TickerSelector(ticker = "")
            }
        }
    }
}

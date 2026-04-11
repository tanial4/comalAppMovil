package com.example.comalapp.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

enum class AppTextFieldType {
    Text,
    Number,
    Email,
    Password,
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    type: AppTextFieldType = AppTextFieldType.Text,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val keyboardOptions = KeyboardOptions(
        keyboardType = when (type) {
            AppTextFieldType.Text     -> KeyboardType.Text
            AppTextFieldType.Number   -> KeyboardType.Number
            AppTextFieldType.Email    -> KeyboardType.Email
            AppTextFieldType.Password -> KeyboardType.Password
        },
        imeAction = imeAction,
    )

    val visualTransformation = when {
        type == AppTextFieldType.Password && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val trailingIcon: (@Composable () -> Unit)? = when (type) {
        AppTextFieldType.Password -> {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Outlined.Visibility
                        else
                            Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        else -> null
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        placeholder = if (placeholder.isNotEmpty()) {
            {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else null,
        isError = isError,
        supportingText = if (isError && errorMessage.isNotEmpty()) {
            {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else null,
        enabled = enabled,
        singleLine = type != AppTextFieldType.Text,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
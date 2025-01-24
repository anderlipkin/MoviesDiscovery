package com.example.moviesdiscovery.core.ui.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource

sealed class UiStringValue {
    data class DynamicString(val value: String) : UiStringValue()

    class StringResource(
        @StringRes val resId: Int,
        val args: List<Any>
    ) : UiStringValue()

    @ReadOnlyComposable
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, args)
        }
    }
}

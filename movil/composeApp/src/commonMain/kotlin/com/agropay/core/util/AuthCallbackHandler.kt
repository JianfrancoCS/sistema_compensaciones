package com.agropay.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Handler para comunicar el authorization code desde el deep link al ViewModel
 */
object AuthCallbackHandler {
    private val _authCodeFlow = MutableSharedFlow<String>(replay = 0)
    val authCodeFlow: SharedFlow<String> = _authCodeFlow.asSharedFlow()

    suspend fun handleAuthCode(code: String) {
        _authCodeFlow.emit(code)
    }
}
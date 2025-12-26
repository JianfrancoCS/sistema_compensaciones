package com.agropay.core.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Implementación iOS de ViewModel usando CoroutineScope simple
 */
actual abstract class ViewModel actual constructor() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    actual val viewModelScope: CoroutineScope
        get() = scope
    
    actual open fun onCleared() {
        scope.cancel()
    }
}


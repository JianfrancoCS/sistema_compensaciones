package com.agropay.core.presentation

import kotlinx.coroutines.CoroutineScope

/**
 * Expect declaration para ViewModel multiplataforma
 * En Android usa androidx.lifecycle.ViewModel
 * En iOS usa una implementación simple con CoroutineScope
 */
expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope
    
    protected open fun onCleared()
}


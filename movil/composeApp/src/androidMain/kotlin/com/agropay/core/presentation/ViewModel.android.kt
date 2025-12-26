package com.agropay.core.presentation

import androidx.lifecycle.ViewModel as AndroidViewModel
import androidx.lifecycle.viewModelScope as androidViewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * Implementación Android de ViewModel usando androidx.lifecycle.ViewModel
 */
actual abstract class ViewModel actual constructor() : AndroidViewModel() {
    actual val viewModelScope: CoroutineScope
        get() = androidViewModelScope
    
    actual override fun onCleared() {
        super.onCleared()
    }
}


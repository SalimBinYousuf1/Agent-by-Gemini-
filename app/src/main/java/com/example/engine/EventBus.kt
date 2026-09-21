package com.example.engine

import com.example.data.model.ScreenContext
import com.example.data.model.TriggerEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object EventBus {
    private val _screenContextFlow = MutableSharedFlow<ScreenContext>(extraBufferCapacity = 64)
    val screenContextFlow: SharedFlow<ScreenContext> = _screenContextFlow.asSharedFlow()

    private val _triggerEventFlow = MutableSharedFlow<TriggerEvent>(extraBufferCapacity = 64)
    val triggerEventFlow: SharedFlow<TriggerEvent> = _triggerEventFlow.asSharedFlow()

    private val _currentScreenContext = MutableStateFlow<ScreenContext?>(null)
    val currentScreenContext: StateFlow<ScreenContext?> = _currentScreenContext.asStateFlow()

    fun emitScreenContext(context: ScreenContext) {
        _currentScreenContext.value = context
        _screenContextFlow.tryEmit(context)
    }

    fun emitTriggerEvent(event: TriggerEvent) {
        _triggerEventFlow.tryEmit(event)
    }
}

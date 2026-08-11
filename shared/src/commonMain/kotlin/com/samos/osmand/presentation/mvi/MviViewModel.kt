package com.samos.osmand.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel class that implements the Model-View-Intent (MVI) architectural pattern.
 * It provides a unidirectional data flow (UDF) by managing a single UI State and
 * a stream of single-use Side Effects.
 *
 * @param S the type of the UI State which represents the current visual representation of the screen.
 * @param I the type of the User Intent which represents user actions or system events.
 * @param E the type of the UI Effect which represents one-off events like navigation, toasts, or dialogs.
 * @property initialState the starting state of the UI when the ViewModel is first initialized.
 */
abstract class MviViewModel<S : MviState, in I : MviIntent, E : MviEffect>(
    val initialState: S,
) : ViewModel() {

    /**
     * A read-only [StateFlow] emitting the persistent UI state.
     * The UI layer should collect this to render the screen elements.
     */
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> by lazy { _state.asStateFlow() }

    /**
     * A hot [kotlinx.coroutines.flow.Flow] emitting single-use side effects.
     * Uses a buffered channel underneath to guarantee delivery even during configuration changes.
     *
     * Send here one-off events
     * - `ShowToast(val message: String)`
     * - `ShowSnackbar(val message: String, val actionLabel: String?)`
     * - `ShowErrorDialog(val exception: Throwable)`
     * - `OpenDetails(val itemId: String)`
     * - `CloseKeyboard`
     * - `TriggerVibration`
     * - `RequestPermissions(val permissions: List<String>)`
     */
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val currentState: S
        get() = _state.value

    /**
     * Processes incoming user intents or system actions.
     * Must be implemented by subclasses to define the screen's business logic.
     *
     * @param intent the specific action triggered by the UI layer.
     */
    abstract fun handleIntent(intent: I)

    /**
     * Updates the persistent UI state in a thread-safe manner using an atomic transformation.
     * Accessible only within subclasses.
     *
     * @param transform a lambda function that takes the current state and returns the updated state.
     */
    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    /**
     * Dispatches a single-use side effect to the UI layer asynchronously.
     * Accessible only within subclasses.
     *
     * @param effect the event to be delivered to the UI layer (e.g., navigation or snackbar).
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}

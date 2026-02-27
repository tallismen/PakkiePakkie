package nl.designlama.pakkiepakkie.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import nl.designlama.pakkiepakkie.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<UI_STATE : UIState, UI_EVENT : UIEvent, NAVIGATION_DIRECTIONS: UIDirections>(private val reemitNavigation: Boolean = false) : ViewModel(), CoroutineScope {
    protected val _state = MutableStateFlow(this.defaultUIState())
    val state = _state.asStateFlow()

    private val _directions = MutableSharedFlow<NAVIGATION_DIRECTIONS>(replay = if (reemitNavigation) 1 else 0)
    val directions = _directions.asSharedFlow()

    private val parentJob = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Default + parentJob

    protected fun navigate(directions: NAVIGATION_DIRECTIONS) {
        viewModelScope.launch {
            _directions.emit(directions)
        }
    }

    protected abstract fun defaultUIState(): UI_STATE

    @CallSuper
    open fun onEvent(event: UI_EVENT) {
        Logger.v("${this::class.simpleName}", "Received event: $event")
    }

    override fun onCleared() {
        parentJob.cancel()
        super.onCleared()
    }
}

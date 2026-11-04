package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.datastore.OnboardingRepository

data class TestMenuState(
    val onboardingSeen: Boolean = false,
) : UIState

sealed interface TestMenuEvent : UIEvent {
    data class OnOnboardingSeenChanged(val seen: Boolean) : TestMenuEvent
}

class TestMenuViewModel(
    private val onboardingRepository: OnboardingRepository,
) : BaseViewModel<TestMenuState, TestMenuEvent, TestMenuDirections>() {

    init {
        viewModelScope.launch {
            onboardingRepository.onboardingSeen.collectLatest { seen ->
                _state.value = _state.value.copy(onboardingSeen = seen)
            }
        }
    }

    override fun defaultUIState(): TestMenuState = TestMenuState()

    override fun onEvent(event: TestMenuEvent) {
        super.onEvent(event)
        when (event) {
            is TestMenuEvent.OnOnboardingSeenChanged -> {
                viewModelScope.launch {
                    onboardingRepository.setOnboardingSeen(event.seen)
                }
            }
        }
    }
}

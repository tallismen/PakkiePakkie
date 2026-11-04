package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.datastore.OnboardingRepository

data class OnboardingState(
    val pageCount: Int = 3,
) : UIState

sealed interface OnboardingEvent : UIEvent {
    data object OnClose : OnboardingEvent
}

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
) : BaseViewModel<OnboardingState, OnboardingEvent, OnboardingDirections>() {

    override fun defaultUIState(): OnboardingState = OnboardingState()

    override fun onEvent(event: OnboardingEvent) {
        super.onEvent(event)
        when (event) {
            OnboardingEvent.OnClose -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setOnboardingSeen(true)
            navigate(OnboardingDirections.Finish)
        }
    }
}

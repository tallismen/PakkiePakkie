package nl.designlama.pakkiepakkie.ui

import nl.designlama.pakkiepakkie.base.UIDirections

sealed interface OnboardingDirections : UIDirections {
    data object Finish : OnboardingDirections
}

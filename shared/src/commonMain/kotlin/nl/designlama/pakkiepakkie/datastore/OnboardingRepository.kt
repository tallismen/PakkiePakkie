package nl.designlama.pakkiepakkie.datastore

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class OnboardingRepository(
    private val preferencesRepository: PreferencesRepository,
) {
    val onboardingSeen: Flow<Boolean> =
        preferencesRepository.getBoolean(PreferencesKeys.ONBOARDING_SEEN, defaultValue = false)

    suspend fun setOnboardingSeen(seen: Boolean) {
        preferencesRepository.saveBoolean(PreferencesKeys.ONBOARDING_SEEN, seen)
    }
}

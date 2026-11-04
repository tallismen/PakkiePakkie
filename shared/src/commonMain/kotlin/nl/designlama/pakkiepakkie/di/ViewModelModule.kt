package nl.designlama.pakkiepakkie.di

import nl.designlama.pakkiepakkie.ui.HomeViewModel
import nl.designlama.pakkiepakkie.ui.OnboardingViewModel
import nl.designlama.pakkiepakkie.ui.SettingsViewModel
import nl.designlama.pakkiepakkie.ui.TestMenuViewModel
import nl.designlama.pakkiepakkie.ui.VehicleDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun viewModelModule() = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { TestMenuViewModel(get()) }
    viewModel { (kenteken: String) ->
        VehicleDetailViewModel(
            vehicleLicenseRepository = get(),
            userVehicleRepository = get(),
            reviewRepository = get(),
            kenteken = kenteken,
        )
    }
}

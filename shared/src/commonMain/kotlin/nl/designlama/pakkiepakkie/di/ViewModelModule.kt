package nl.designlama.pakkiepakkie.di

import nl.designlama.pakkiepakkie.ui.HomeViewModel
import nl.designlama.pakkiepakkie.ui.VehicleDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun viewModelModule() = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { (kenteken: String) ->
        VehicleDetailViewModel(
            vehicleLicenseRepository = get(),
            userVehicleRepository = get(),
            kenteken = kenteken,
        )
    }
}

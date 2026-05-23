package nl.designlama.pakkiepakkie.ui

import nl.designlama.pakkiepakkie.base.UIDirections

sealed interface HomeDirections : UIDirections {
    data class OpenVehicleDetail(val kenteken: String) : HomeDirections
}

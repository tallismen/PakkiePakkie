package nl.designlama.pakkiepakkie.ui

import nl.designlama.pakkiepakkie.base.UIDirections

sealed interface SearchDirections : UIDirections {
    data class OpenVehicleDetail(val kenteken: String) : SearchDirections
    data object OpenProfile : SearchDirections
}

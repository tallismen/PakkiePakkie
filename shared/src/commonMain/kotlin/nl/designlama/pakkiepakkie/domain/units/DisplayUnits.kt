package nl.designlama.pakkiepakkie.domain.units

enum class UnitPreset {
    Standaard,
    Metric,
    Imperial,
    PakkiePakkie,
}

enum class PowerUnit {
    Kw,
    Hp,
    Pakkies,
}

enum class WeightUnit {
    Kg,
    Lbs,
    Slippers,
}

data class UnitPreferences(
    val preset: UnitPreset = UnitPreset.Standaard,
    val powerUnit: PowerUnit = PowerUnit.Kw,
    val weightUnit: WeightUnit = WeightUnit.Kg,
)

object UnitConversions {
    const val KW_TO_PK = 1.36
    const val KW_TO_HP = 1.34102
    const val KW_PER_PAKKIE = 40.0
    const val KG_TO_LBS = 2.20462
    const val KG_PER_SLIPPER = 0.25

    fun presetUnits(preset: UnitPreset): Pair<PowerUnit, WeightUnit> = when (preset) {
        UnitPreset.Standaard -> PowerUnit.Kw to WeightUnit.Kg
        UnitPreset.Metric -> PowerUnit.Kw to WeightUnit.Kg
        UnitPreset.Imperial -> PowerUnit.Hp to WeightUnit.Lbs
        UnitPreset.PakkiePakkie -> PowerUnit.Pakkies to WeightUnit.Slippers
    }
}

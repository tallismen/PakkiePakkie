package nl.designlama.pakkiepakkie.domain.units

import kotlin.math.pow
import kotlin.math.roundToInt
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import org.koin.core.annotation.Single

@Single
class VehicleDisplayFormatter {

    fun formatPower(kw: Double?, preferences: UnitPreferences): String {
        if (kw == null) return "—"
        return when {
            preferences.preset == UnitPreset.Standaard && preferences.powerUnit == PowerUnit.Kw -> {
                val kwTxt = kw.roundToInt()
                val pk = (kw * UnitConversions.KW_TO_PK).roundToInt()
                "$kwTxt kW ($pk pk)"
            }
            preferences.powerUnit == PowerUnit.Kw -> "${kw.roundToInt()} kW"
            preferences.powerUnit == PowerUnit.Hp -> {
                val hp = (kw * UnitConversions.KW_TO_HP).roundToInt()
                "$hp HP"
            }
            preferences.powerUnit == PowerUnit.Pakkies -> {
                val pakkies = kw / UnitConversions.KW_PER_PAKKIE
                formatDecimal(pakkies, 1) + " Pakkies"
            }
            else -> "${kw.roundToInt()} kW"
        }
    }

    fun formatWeight(kg: Int?, preferences: UnitPreferences): String {
        if (kg == null) return "—"
        return when (preferences.weightUnit) {
            WeightUnit.Kg -> "$kg kg"
            WeightUnit.Lbs -> {
                val lbs = (kg * UnitConversions.KG_TO_LBS).roundToInt()
                "$lbs lbs"
            }
            WeightUnit.Slippers -> {
                val slippers = (kg / UnitConversions.KG_PER_SLIPPER).roundToInt()
                "$slippers slippers"
            }
        }
    }

    fun powerLabel(preferences: UnitPreferences): String = when (preferences.powerUnit) {
        PowerUnit.Kw -> if (preferences.preset == UnitPreset.Standaard) "Vermogen" else "Vermogen (kW)"
        PowerUnit.Hp -> "Vermogen (HP)"
        PowerUnit.Pakkies -> "Vermogen (Pakkies)"
    }

    fun weightLabel(preferences: UnitPreferences, rijklaar: Boolean): String {
        val prefix = if (rijklaar) "Rijklaar" else "Ledig"
        return when (preferences.weightUnit) {
            WeightUnit.Kg -> "$prefix (kg)"
            WeightUnit.Lbs -> "$prefix (lbs)"
            WeightUnit.Slippers -> "$prefix (slippers)"
        }
    }

    fun pkPerKilo(info: VehicleLicensePlateInfo): Double? {
        info.vermogenMassaRijklaar?.let { return it * UnitConversions.KW_TO_PK }
        val kw = info.vermogenKw ?: return null
        val kg = info.massaRijklaarKg ?: return null
        return (kw * UnitConversions.KW_TO_PK) / kg
    }

    fun formatPkPerKilo(info: VehicleLicensePlateInfo): String {
        val value = pkPerKilo(info) ?: return "—"
        return formatDecimal(value, 2)
    }

    private fun formatDecimal(value: Double, decimals: Int): String {
        val factor = when (decimals) {
            0 -> 1.0
            1 -> 10.0
            2 -> 100.0
            else -> 10.0.pow(decimals.toDouble())
        }
        val rounded = (value * factor).roundToInt() / factor
        val whole = rounded.toInt()
        if (decimals == 0 || whole.toDouble() == rounded) return whole.toString()
        val fractional = ((rounded - whole) * factor).roundToInt()
        return "$whole.${fractional.toString().padStart(decimals, '0')}"
    }
}

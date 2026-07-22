package nl.designlama.pakkiepakkie.domain.units

import kotlin.math.pow
import kotlin.math.roundToInt
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import org.koin.core.annotation.Single

@Single
class VehicleDisplayFormatter {

    fun formatPower(kw: Double?, preferences: UnitPreferences): String {
        if (kw == null) return UnitSymbols.EM_DASH
        return when {
            preferences.preset == UnitPreset.Standaard && preferences.powerUnit == PowerUnit.Kw -> {
                val kwTxt = kw.roundToInt()
                val pk = (kw * UnitConversions.KW_TO_PK).roundToInt()
                "$kwTxt ${UnitSymbols.KW} ($pk ${UnitSymbols.PK})"
            }
            preferences.powerUnit == PowerUnit.Kw -> "${kw.roundToInt()} ${UnitSymbols.KW}"
            preferences.powerUnit == PowerUnit.Hp -> {
                val hp = (kw * UnitConversions.KW_TO_HP).roundToInt()
                "$hp ${UnitSymbols.HP}"
            }
            preferences.powerUnit == PowerUnit.Pakkies -> {
                val pakkies = kw / UnitConversions.KW_PER_PAKKIE
                "${formatDecimal(pakkies, 1)} ${UnitSymbols.PAKKIES}"
            }
            else -> "${kw.roundToInt()} ${UnitSymbols.KW}"
        }
    }

    fun formatWeight(kg: Int?, preferences: UnitPreferences): String {
        if (kg == null) return UnitSymbols.EM_DASH
        return when (preferences.weightUnit) {
            WeightUnit.Kg -> "$kg ${UnitSymbols.KG}"
            WeightUnit.Lbs -> {
                val lbs = (kg * UnitConversions.KG_TO_LBS).roundToInt()
                "$lbs ${UnitSymbols.LBS}"
            }
            WeightUnit.Slippers -> {
                val slippers = (kg / UnitConversions.KG_PER_SLIPPER).roundToInt()
                "$slippers ${UnitSymbols.SLIPPERS}"
            }
        }
    }

    fun pkPerKilo(info: VehicleLicensePlateInfo): Double? {
        info.vermogenMassaRijklaar?.let { return it * UnitConversions.KW_TO_PK }
        val kw = info.vermogenKw ?: return null
        val kg = info.massaRijklaarKg ?: return null
        return (kw * UnitConversions.KW_TO_PK) / kg
    }

    fun formatPkPerKilo(info: VehicleLicensePlateInfo): String {
        val value = pkPerKilo(info) ?: return UnitSymbols.EM_DASH
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

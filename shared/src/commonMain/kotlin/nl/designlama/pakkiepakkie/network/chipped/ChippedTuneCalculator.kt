package nl.designlama.pakkiepakkie.network.chipped

import kotlin.math.round
import nl.designlama.pakkiepakkie.network.rdw.RdwFuelLabels
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo

data class ChippedTuneEstimate(
    val stockPk: Int,
    val stage1Pk: Int,
    val gainPk: Int,
) {
    val stage1Kw: Double get() = stage1Pk / 1.36
}

object ChippedTuneCalculator {

    fun estimate(info: VehicleLicensePlateInfo): ChippedTuneEstimate? {
        val stockPk = info.vermogenPk?.let { round(it).toInt() } ?: return null
        if (!canBeChipped(info)) return null
        val multiplier = when {
            info.brandstofOmschrijvingen.any { it.equals(RdwFuelLabels.DIESEL, ignoreCase = true) } -> 1.18
            info.brandstofOmschrijvingen.any {
                it.contains(RdwFuelLabels.BENZINE, ignoreCase = true) ||
                    it.contains(RdwFuelLabels.GAS, ignoreCase = true)
            } -> 1.15
            else -> 1.12
        }
        val stage1Pk = (stockPk * multiplier).toInt()
        return ChippedTuneEstimate(
            stockPk = stockPk,
            stage1Pk = stage1Pk,
            gainPk = stage1Pk - stockPk,
        )
    }

    fun canBeChipped(info: VehicleLicensePlateInfo): Boolean {
        val hasEvOnly = info.brandstofOmschrijvingen.any {
            it.equals(RdwFuelLabels.ELEKTRICITEIT, ignoreCase = true)
        } && info.brandstofOmschrijvingen.none {
            it.contains(RdwFuelLabels.BENZINE, ignoreCase = true) ||
                it.contains(RdwFuelLabels.DIESEL, ignoreCase = true) ||
                it.contains(RdwFuelLabels.GAS, ignoreCase = true)
        }
        return !hasEvOnly && (info.vermogenPk ?: 0.0) > 0.0
    }
}

package nl.designlama.pakkiepakkie.network.rdw

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

/**
 * Heuristic 0–100 sprint proxy and win probability vs another vehicle.
 * Constants are tunable in one place.
 */
object PakkiePakkieCalculator {

    private const val LOGISTIC_K = 2.8
    private const val MIN_PCT = 5f
    private const val MAX_PCT = 95f
    private const val MIN_MASS_KG = 400.0
    private const val MIN_KW = 15.0

    /** Multiplier for launch / drivetrain (1.0 = neutral). */
    fun transmissionMultiplier(code: String?, brandstoffen: List<String>): Double {
        if (isPureEv(brandstoffen)) return 1.0
        return when (code?.trim()?.uppercase()) {
            "M" -> 1.045
            "C" -> 1.025
            "G", "D" -> 1.03
            "A", null, "" -> 1.0
            else -> 1.0
        }
    }

    fun sprintIndex(info: VehicleLicensePlateInfo): Double {
        val kw = (info.vermogenKw ?: return 0.01).coerceAtLeast(MIN_KW)
        val kg = (info.massaRijklaarKg?.toDouble() ?: return 0.01).coerceAtLeast(MIN_MASS_KG)
        val base = kw / kg * 1000.0
        val fuel = fuelMultiplier(info.brandstofOmschrijvingen, info.hybridKlasse)
        val trans = transmissionMultiplier(info.versnellingsbakCode, info.brandstofOmschrijvingen)
        return base * fuel * trans
    }

    /**
     * Estimated chance **(0–100)** that [my] wins a 0–100 sprint vs [other].
     */
    fun winProbabilityPercent(my: VehicleLicensePlateInfo, other: VehicleLicensePlateInfo): Float {
        val a = sprintIndex(my)
        val b = sprintIndex(other)
        return winProbabilityFromIndices(a, b)
    }

    internal fun winProbabilityFromIndices(myIndex: Double, otherIndex: Double): Float {
        if (myIndex <= 0.0 || otherIndex <= 0.0) return 50f
        val ratio = myIndex / otherIndex
        val logR = ln(max(ratio, 1e-9))
        val p = 1.0 / (1.0 + exp(-LOGISTIC_K * logR))
        return (p * 100.0).toFloat().coerceIn(MIN_PCT, MAX_PCT)
    }

    private fun fuelMultiplier(brandstoffen: List<String>, hybridKlasse: String?): Double {
        if (isPureEv(brandstoffen)) return 1.12
        if (isHybridish(brandstoffen, hybridKlasse)) return 1.06
        if (brandstoffen.any { it.equals("Diesel", ignoreCase = true) }) return 0.985
        return 1.0
    }

    private fun isPureEv(brandstoffen: List<String>): Boolean {
        val hasEl = brandstoffen.any { it.equals("Elektriciteit", ignoreCase = true) }
        if (!hasEl) return false
        val hasIce = brandstoffen.any { label ->
            val l = label.lowercase()
            l.contains("benzine") || l.contains("diesel") || l.contains("gas") || l.contains("hybride")
        }
        return !hasIce
    }

    private fun isHybridish(brandstoffen: List<String>, hybridKlasse: String?): Boolean {
        if (!hybridKlasse.isNullOrBlank()) return true
        if (brandstoffen.any { it.contains("Hybride", ignoreCase = true) }) return true
        val hasEl = brandstoffen.any { it.equals("Elektriciteit", ignoreCase = true) }
        val hasIce = brandstoffen.any { label ->
            val l = label.lowercase()
            l.contains("benzine") || l.contains("diesel") || l.contains("gas")
        }
        return hasEl && hasIce
    }
}

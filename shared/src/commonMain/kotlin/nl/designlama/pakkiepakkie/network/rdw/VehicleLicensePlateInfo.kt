package nl.designlama.pakkiepakkie.network.rdw

/**
 * Parses RDW numeric strings (comma or dot decimal) to kW.
 */
object RdwPowerParser {

    fun parsePowerKw(raw: String?): Double? {
        val s = raw?.trim()?.replace(',', '.')?.takeIf { it.isNotEmpty() } ?: return null
        return s.toDoubleOrNull()
    }

    fun maxPowerKwFromFuelRows(rows: List<RdwVehicleFuelDto>): Double? =
        rows.flatMap { row ->
            listOfNotNull(
                parsePowerKw(row.nettoMaximumVermogen),
                parsePowerKw(row.nominaalContinuMaximumVermogen),
            )
        }.maxOrNull()

    /**
     * Official type-approval kW from RDW (vermogen/massa rijklaar × massa rijklaar).
     * Falls back to max per fuel row when ratio or mass is missing.
     */
    fun resolveOfficialPowerKw(
        vermogenMassaRijklaar: Double?,
        massaRijklaarKg: Int?,
        fuelRows: List<RdwVehicleFuelDto>,
    ): Double? {
        if (vermogenMassaRijklaar != null && massaRijklaarKg != null) {
            return vermogenMassaRijklaar * massaRijklaarKg
        }
        return maxPowerKwFromFuelRows(fuelRows)
    }
}

object RdwNumberParser {
    fun parseInt(raw: String?): Int? =
        raw?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()

    fun parseDouble(raw: String?): Double? {
        val s = raw?.trim()?.replace(',', '.')?.takeIf { it.isNotEmpty() } ?: return null
        return s.toDoubleOrNull()
    }
}

data class VehicleLicensePlateInfo(
    val kenteken: String,
    val merk: String,
    val handelsbenaming: String,
    val massaRijklaarKg: Int?,
    val massaLedigKg: Int?,
    val cilinderinhoudCc: Int?,
    /** RDW kW/kg rijklaar when present. */
    val vermogenMassaRijklaar: Double?,
    val vermogenKw: Double?,
    /** Distinct brandstof_omschrijving values from fuel rows (order preserved). */
    val brandstofOmschrijvingen: List<String>,
    val hybridKlasse: String?,
    /** RDW `codetypeversnellingsbak` e.g. A, M, C. */
    val versnellingsbakCode: String?,
    val aantalVersnellingen: Int?,
) {
    val vermogenPk: Double?
        get() = vermogenKw?.times(1.36)

    /** Enough persisted fields to show basic card without calling RDW again. */
    fun hasSufficientCachedFields(): Boolean =
        merk.isNotBlank() ||
            handelsbenaming.isNotBlank() ||
            massaRijklaarKg != null ||
            vermogenKw != null
}

internal object FuelSummaryMapper {
    fun fromFuelRows(rows: List<RdwVehicleFuelDto>): Pair<List<String>, String?> {
        val labels = LinkedHashSet<String>()
        for (row in rows) {
            val b = row.brandstofOmschrijving?.trim()?.takeIf { it.isNotEmpty() } ?: continue
            labels.add(b)
        }
        val hybrid = rows.firstNotNullOfOrNull { it.klasseHybrideElektrischVoertuig?.trim()?.takeIf { s -> s.isNotEmpty() } }
        return labels.toList() to hybrid
    }
}

class RdwNotFoundException(val kenteken: String) : Exception("Geen voertuig gevonden voor kenteken $kenteken")

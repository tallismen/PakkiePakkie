package nl.designlama.pakkiepakkie.network.rdw

/**
 * RDW Open Data dataset paths and query parameter names.
 */
object RdwApi {
    const val DATASET_VEHICLE_MAIN = "m9d7-ebf2.json"
    const val DATASET_VEHICLE_FUEL = "8ys7-d773.json"
    const val DATASET_TGK_GEARBOX = "7rjk-eycs.json"

    const val PARAM_KENTEKEN = "kenteken"
    const val PARAM_TYPEGOEDKEURING = "typegoedkeuringsnummer"
    const val PARAM_VARIANT = "codevarianttgk"
    const val PARAM_UITVOERING = "codeuitvoeringtgk"

    const val HEADER_APP_TOKEN = "X-App-Token"
}

/**
 * RDW brandstof labels used for matching (case-insensitive).
 */
object RdwFuelLabels {
    const val DIESEL = "Diesel"
    const val BENZINE = "Benzine"
    const val ELEKTRICITEIT = "Elektriciteit"
    const val HYBRIDE = "Hybride"
    const val GAS = "gas"
}

/**
 * RDW `codetypeversnellingsbak` values.
 */
object RdwTransmissionCodes {
    const val AUTOMATIC = "A"
    const val MANUAL = "M"
    const val CVT = "C"
    const val DCT_G = "G"
    const val DCT_D = "D"
}

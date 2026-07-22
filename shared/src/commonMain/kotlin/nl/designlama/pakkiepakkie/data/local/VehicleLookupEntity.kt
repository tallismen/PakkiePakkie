package nl.designlama.pakkiepakkie.data.local

data class VehicleLookupEntity(
    val kenteken: String,
    val merk: String,
    val handelsbenaming: String,
    val massaRijklaarKg: Int?,
    val vermogenKw: Double?,
    val vermogenPk: Double?,
    val massaLedigKg: Int?,
    val cilinderinhoudCc: Int?,
    val vermogenMassaRijklaar: Double?,
    val primaryBrandstof: String?,
    val hybridKlasse: String?,
    /** JSON array of distinct brandstof_omschrijving (same order as API). */
    val brandstofJson: String?,
    val versnellingsbakCode: String?,
    val aantalVersnellingen: Int?,
    val maximaleConstructiesnelheidKmh: Int?,
    val isChipped: Boolean = false,
    val dataVersion: Int = VehicleLookupDataVersion.LEGACY,
    val lastViewedAt: Long,
    val lastFetchedAt: Long,
)

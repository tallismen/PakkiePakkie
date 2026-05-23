package nl.designlama.pakkiepakkie.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_lookup")
data class VehicleLookupEntity(
    @PrimaryKey val kenteken: String,
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
    @ColumnInfo(defaultValue = "0") val isChipped: Boolean,
    @ColumnInfo(defaultValue = "1") val dataVersion: Int,
    val lastViewedAt: Long,
    val lastFetchedAt: Long,
)

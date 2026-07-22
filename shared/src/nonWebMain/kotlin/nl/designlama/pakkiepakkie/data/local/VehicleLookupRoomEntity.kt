package nl.designlama.pakkiepakkie.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_lookup")
data class VehicleLookupRoomEntity(
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
    val brandstofJson: String?,
    val versnellingsbakCode: String?,
    val aantalVersnellingen: Int?,
    val maximaleConstructiesnelheidKmh: Int?,
    @ColumnInfo(defaultValue = "0") val isChipped: Boolean,
    @ColumnInfo(defaultValue = "1") val dataVersion: Int,
    val lastViewedAt: Long,
    val lastFetchedAt: Long,
)

fun VehicleLookupRoomEntity.toDomain(): VehicleLookupEntity =
    VehicleLookupEntity(
        kenteken = kenteken,
        merk = merk,
        handelsbenaming = handelsbenaming,
        massaRijklaarKg = massaRijklaarKg,
        vermogenKw = vermogenKw,
        vermogenPk = vermogenPk,
        massaLedigKg = massaLedigKg,
        cilinderinhoudCc = cilinderinhoudCc,
        vermogenMassaRijklaar = vermogenMassaRijklaar,
        primaryBrandstof = primaryBrandstof,
        hybridKlasse = hybridKlasse,
        brandstofJson = brandstofJson,
        versnellingsbakCode = versnellingsbakCode,
        aantalVersnellingen = aantalVersnellingen,
        maximaleConstructiesnelheidKmh = maximaleConstructiesnelheidKmh,
        isChipped = isChipped,
        dataVersion = dataVersion,
        lastViewedAt = lastViewedAt,
        lastFetchedAt = lastFetchedAt,
    )

fun VehicleLookupEntity.toRoom(): VehicleLookupRoomEntity =
    VehicleLookupRoomEntity(
        kenteken = kenteken,
        merk = merk,
        handelsbenaming = handelsbenaming,
        massaRijklaarKg = massaRijklaarKg,
        vermogenKw = vermogenKw,
        vermogenPk = vermogenPk,
        massaLedigKg = massaLedigKg,
        cilinderinhoudCc = cilinderinhoudCc,
        vermogenMassaRijklaar = vermogenMassaRijklaar,
        primaryBrandstof = primaryBrandstof,
        hybridKlasse = hybridKlasse,
        brandstofJson = brandstofJson,
        versnellingsbakCode = versnellingsbakCode,
        aantalVersnellingen = aantalVersnellingen,
        maximaleConstructiesnelheidKmh = maximaleConstructiesnelheidKmh,
        isChipped = isChipped,
        dataVersion = dataVersion,
        lastViewedAt = lastViewedAt,
        lastFetchedAt = lastFetchedAt,
    )

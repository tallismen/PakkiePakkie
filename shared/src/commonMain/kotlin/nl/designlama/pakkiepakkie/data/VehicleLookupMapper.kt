package nl.designlama.pakkiepakkie.data

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo

private val json = Json { ignoreUnknownKeys = true }
private val stringListSerializer = ListSerializer(String.serializer())

internal fun encodeBrandstofJson(brandstoffen: List<String>): String? =
    if (brandstoffen.isEmpty()) null else json.encodeToString(stringListSerializer, brandstoffen)

internal fun decodeBrandstofJson(raw: String?): List<String> =
    raw?.takeIf { it.isNotBlank() }?.let {
        runCatching { json.decodeFromString(stringListSerializer, it) }.getOrElse { emptyList() }
    }.orEmpty()

fun VehicleLookupEntity.toVehicleLicensePlateInfo(): VehicleLicensePlateInfo =
    VehicleLicensePlateInfo(
        kenteken = kenteken,
        merk = merk,
        handelsbenaming = handelsbenaming,
        massaRijklaarKg = massaRijklaarKg,
        massaLedigKg = massaLedigKg,
        cilinderinhoudCc = cilinderinhoudCc,
        vermogenMassaRijklaar = vermogenMassaRijklaar,
        vermogenKw = vermogenKw,
        brandstofOmschrijvingen = decodeBrandstofJson(brandstofJson),
        hybridKlasse = hybridKlasse,
        versnellingsbakCode = versnellingsbakCode,
        aantalVersnellingen = aantalVersnellingen,
        maximaleConstructiesnelheidKmh = maximaleConstructiesnelheidKmh,
    )

fun VehicleLicensePlateInfo.toVehicleLookupEntity(
    lastViewedAt: Long,
    lastFetchedAt: Long,
    dataVersion: Int = VehicleLookupDataVersion.V3,
    isChipped: Boolean = false,
): VehicleLookupEntity =
    VehicleLookupEntity(
        kenteken = kenteken,
        merk = merk,
        handelsbenaming = handelsbenaming,
        massaRijklaarKg = massaRijklaarKg,
        vermogenKw = vermogenKw,
        vermogenPk = vermogenKw?.times(1.36),
        massaLedigKg = massaLedigKg,
        cilinderinhoudCc = cilinderinhoudCc,
        vermogenMassaRijklaar = vermogenMassaRijklaar,
        primaryBrandstof = brandstofOmschrijvingen.firstOrNull(),
        hybridKlasse = hybridKlasse,
        brandstofJson = encodeBrandstofJson(brandstofOmschrijvingen),
        versnellingsbakCode = versnellingsbakCode,
        aantalVersnellingen = aantalVersnellingen,
        maximaleConstructiesnelheidKmh = maximaleConstructiesnelheidKmh,
        isChipped = isChipped,
        dataVersion = dataVersion,
        lastViewedAt = lastViewedAt,
        lastFetchedAt = lastFetchedAt,
    )

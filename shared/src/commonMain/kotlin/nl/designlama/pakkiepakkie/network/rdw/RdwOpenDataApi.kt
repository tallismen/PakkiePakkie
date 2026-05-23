package nl.designlama.pakkiepakkie.network.rdw

import PakkiePakkie.shared.BuildConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import nl.designlama.pakkiepakkie.utils.AppConfig
import org.koin.core.annotation.Single

@Single
class RdwOpenDataApi(
    private val appConfig: AppConfig,
) {
    private val httpClient by lazy {
        createRdwHttpClient(
            debug = appConfig.debug,
            appToken = BuildConfig.RDW_APP_TOKEN,
            baseUrl = BuildConfig.RDW_OPEN_DATA_BASE_URL,
        )
    }

    suspend fun fetchByKenteken(rawInput: String): VehicleLicensePlateInfo {
        val norm = sanitizeLicensePlate(rawInput)
        if (norm.length != 6) {
            throw IllegalArgumentException("Kenteken moet 6 tekens zijn")
        }

        return coroutineScope {
            val mainDeferred = async {
                httpClient.get("m9d7-ebf2.json") {
                    parameter("kenteken", norm)
                }.body<List<RdwVehicleMainDto>>()
            }
            val fuelDeferred = async {
                runCatching {
                    httpClient.get("8ys7-d773.json") {
                        parameter("kenteken", norm)
                    }.body<List<RdwVehicleFuelDto>>()
                }.getOrElse { emptyList() }
            }

            val mainRows = mainDeferred.await()
            val mainRow = mainRows.firstOrNull() ?: throw RdwNotFoundException(norm)
            val fuelRows = fuelDeferred.await()

            val tgkDeferred = async {
                runCatching {
                    val tgk = mainRow.typegoedkeuringsnummer?.trim()?.takeIf { it.isNotEmpty() }
                    val v = mainRow.variant?.trim()?.takeIf { it.isNotEmpty() }
                    val u = mainRow.uitvoering?.trim()?.takeIf { it.isNotEmpty() }
                    if (tgk == null || v == null || u == null) return@runCatching emptyList()
                    httpClient.get("7rjk-eycs.json") {
                        parameter("typegoedkeuringsnummer", tgk)
                        parameter("codevarianttgk", v)
                        parameter("codeuitvoeringtgk", u)
                    }.body<List<RdwTgkVersnellingDto>>()
                }.getOrElse { emptyList() }
            }
            val tgkRows = tgkDeferred.await()
            val tgkPick = TgkVersnellingResolver.resolve(
                typegoedkeuringsnummer = mainRow.typegoedkeuringsnummer,
                variant = mainRow.variant,
                uitvoering = mainRow.uitvoering,
                volgnummerWijzigingEuTypegoedkeuring = mainRow.volgnummerWijzigingEuTypegoedkeuring,
                rows = tgkRows,
            )
            val (brandstoffen, hybridKlasse) = FuelSummaryMapper.fromFuelRows(fuelRows)
            val versnellingsbakCode = tgkPick?.codetypeversnellingsbak?.trim()?.takeIf { it.isNotEmpty() }
            val aantalVersnellingen = tgkPick?.aantalversnellingenondergrens?.trim()?.toIntOrNull()
                ?: tgkPick?.aantalversnellingenbovengrens?.trim()?.toIntOrNull()

            val massaRijklaarKg = RdwNumberParser.parseInt(mainRow.massaRijklaar)
            val vermogenMassaRijklaar = RdwNumberParser.parseDouble(mainRow.vermogenMassarijklaar)

            VehicleLicensePlateInfo(
                kenteken = norm,
                merk = mainRow.merk.orEmpty(),
                handelsbenaming = mainRow.handelsbenaming.orEmpty(),
                massaRijklaarKg = massaRijklaarKg,
                massaLedigKg = RdwNumberParser.parseInt(mainRow.massaLedigVoertuig),
                cilinderinhoudCc = RdwNumberParser.parseInt(mainRow.cilinderinhoud),
                vermogenMassaRijklaar = vermogenMassaRijklaar,
                vermogenKw = RdwPowerParser.resolveOfficialPowerKw(
                    vermogenMassaRijklaar = vermogenMassaRijklaar,
                    massaRijklaarKg = massaRijklaarKg,
                    fuelRows = fuelRows,
                ),
                brandstofOmschrijvingen = brandstoffen,
                hybridKlasse = hybridKlasse,
                versnellingsbakCode = versnellingsbakCode,
                aantalVersnellingen = aantalVersnellingen,
            )
        }
    }
}

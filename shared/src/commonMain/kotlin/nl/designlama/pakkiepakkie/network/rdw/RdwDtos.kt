package nl.designlama.pakkiepakkie.network.rdw

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class RdwVehicleMainDto(
    @SerialName("kenteken") val kenteken: String? = null,
    @SerialName("merk") val merk: String? = null,
    @SerialName("handelsbenaming") val handelsbenaming: String? = null,
    @SerialName("massa_rijklaar") val massaRijklaar: String? = null,
    @SerialName("massa_ledig_voertuig") val massaLedigVoertuig: String? = null,
    @SerialName("cilinderinhoud") val cilinderinhoud: String? = null,
    @SerialName("vermogen_massarijklaar") val vermogenMassarijklaar: String? = null,
    @SerialName("typegoedkeuringsnummer") val typegoedkeuringsnummer: String? = null,
    @SerialName("variant") val variant: String? = null,
    @SerialName("uitvoering") val uitvoering: String? = null,
    @SerialName("volgnummer_wijziging_eu_typegoedkeuring") val volgnummerWijzigingEuTypegoedkeuring: String? = null,
    @SerialName("maximale_constructiesnelheid") val maximaleConstructiesnelheid: String? = null,
    @SerialName("opgegeven_maximum_snelheid") val opgegevenMaximumSnelheid: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class RdwVehicleFuelDto(
    @SerialName("kenteken") val kenteken: String? = null,
    /** RDW `8ys7-d773` uses concatenated keys; older views (e.g. `8ys7-zzye`) used underscores. */
    @SerialName("nettomaximumvermogen")
    @JsonNames("netto_maximum_vermogen")
    val nettoMaximumVermogen: String? = null,
    @SerialName("nominaalcontinumaximumvermogen")
    @JsonNames("nominaal_continu_maximum_vermogen")
    val nominaalContinuMaximumVermogen: String? = null,
    @SerialName("brandstof_omschrijving") val brandstofOmschrijving: String? = null,
    @SerialName("klasse_hybride_elektrisch_voertuig")
    @JsonNames("klassehybrideelektrischvoertuig")
    val klasseHybrideElektrischVoertuig: String? = null,
)

@Serializable
data class RdwTgkVersnellingDto(
    @SerialName("typegoedkeuringsnummer") val typegoedkeuringsnummer: String? = null,
    @SerialName("codevarianttgk") val codevarianttgk: String? = null,
    @SerialName("codeuitvoeringtgk") val codeuitvoeringtgk: String? = null,
    @SerialName("volgnummerrevisieuitvoering") val volgnummerrevisieuitvoering: String? = null,
    @SerialName("volgnummerversnelling") val volgnummerversnelling: String? = null,
    @SerialName("codetypeversnellingsbak") val codetypeversnellingsbak: String? = null,
    @SerialName("aantalversnellingenondergrens") val aantalversnellingenondergrens: String? = null,
    @SerialName("aantalversnellingenbovengrens") val aantalversnellingenbovengrens: String? = null,
)

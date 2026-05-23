package nl.designlama.pakkiepakkie.network.rdw

/**
 * Picks the TGK versnellingsbak row for a registered vehicle using keys from [RdwVehicleMainDto].
 */
object TgkVersnellingResolver {

    fun resolve(
        typegoedkeuringsnummer: String?,
        variant: String?,
        uitvoering: String?,
        volgnummerWijzigingEuTypegoedkeuring: String?,
        rows: List<RdwTgkVersnellingDto>,
    ): RdwTgkVersnellingDto? {
        val tgk = typegoedkeuringsnummer?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val v = variant?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val u = uitvoering?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val rev = volgnummerWijzigingEuTypegoedkeuring?.trim()?.takeIf { it.isNotEmpty() }

        val matched = rows.filter { row ->
            row.typegoedkeuringsnummer?.trim() == tgk &&
                row.codevarianttgk?.trim() == v &&
                row.codeuitvoeringtgk?.trim() == u &&
                (rev == null || row.volgnummerrevisieuitvoering?.trim() == rev)
        }
        val primary = matched.filter { it.volgnummerversnelling?.trim() == "1" }
        val poolPrimary = primary.ifEmpty { matched }
        if (poolPrimary.isNotEmpty()) return poolPrimary.firstOrNull()

        val loose = rows.filter { row ->
            row.typegoedkeuringsnummer?.trim() == tgk &&
                row.codevarianttgk?.trim() == v &&
                row.codeuitvoeringtgk?.trim() == u
        }
        val loosePrimary = loose.filter { it.volgnummerversnelling?.trim() == "1" }
        return loosePrimary.firstOrNull() ?: loose.firstOrNull()
    }
}

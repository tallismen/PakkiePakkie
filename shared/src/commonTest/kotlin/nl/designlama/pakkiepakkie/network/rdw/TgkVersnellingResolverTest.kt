package nl.designlama.pakkiepakkie.network.rdw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TgkVersnellingResolverTest {

    private val tgk = "e2*2007/46*0684*34"
    private val variant = "DH2"
    private val uitvoering = "N74XB16A50KB"

    @Test
    fun resolve_prefersVolgnummerVersnellingOne() {
        val rows = listOf(
            RdwTgkVersnellingDto(
                typegoedkeuringsnummer = tgk,
                codevarianttgk = variant,
                codeuitvoeringtgk = uitvoering,
                volgnummerrevisieuitvoering = "0",
                volgnummerversnelling = "1",
                codetypeversnellingsbak = "A",
            ),
            RdwTgkVersnellingDto(
                typegoedkeuringsnummer = tgk,
                codevarianttgk = variant,
                codeuitvoeringtgk = uitvoering,
                volgnummerrevisieuitvoering = "0",
                volgnummerversnelling = "2",
                codetypeversnellingsbak = "M",
            ),
        )
        val pick = TgkVersnellingResolver.resolve(tgk, variant, uitvoering, "0", rows)
        assertEquals("A", pick?.codetypeversnellingsbak)
    }

    @Test
    fun resolve_returnsNullWhenJoinKeysMissing() {
        assertNull(TgkVersnellingResolver.resolve(null, variant, uitvoering, "0", emptyList()))
        assertNull(TgkVersnellingResolver.resolve(tgk, "", uitvoering, "0", emptyList()))
    }

    @Test
    fun resolve_fallsBackWithoutRevisionWhenStrictMatchEmpty() {
        val rows = listOf(
            RdwTgkVersnellingDto(
                typegoedkeuringsnummer = tgk,
                codevarianttgk = variant,
                codeuitvoeringtgk = uitvoering,
                volgnummerrevisieuitvoering = "1",
                volgnummerversnelling = "1",
                codetypeversnellingsbak = "M",
            ),
        )
        val pick = TgkVersnellingResolver.resolve(tgk, variant, uitvoering, "0", rows)
        assertEquals("M", pick?.codetypeversnellingsbak)
    }
}

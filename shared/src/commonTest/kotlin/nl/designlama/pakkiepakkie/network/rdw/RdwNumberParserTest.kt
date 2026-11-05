package nl.designlama.pakkiepakkie.network.rdw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RdwNumberParserTest {

    @Test
    fun parseBouwjaar_yyyyMmDd() {
        assertEquals(2015, RdwNumberParser.parseBouwjaar("20150115"))
    }

    @Test
    fun parseBouwjaar_dashed() {
        assertEquals(2019, RdwNumberParser.parseBouwjaar("2019-06-01"))
    }

    @Test
    fun parseBouwjaar_invalid() {
        assertNull(RdwNumberParser.parseBouwjaar(null))
        assertNull(RdwNumberParser.parseBouwjaar(""))
        assertNull(RdwNumberParser.parseBouwjaar("15"))
        assertNull(RdwNumberParser.parseBouwjaar("1800"))
    }
}

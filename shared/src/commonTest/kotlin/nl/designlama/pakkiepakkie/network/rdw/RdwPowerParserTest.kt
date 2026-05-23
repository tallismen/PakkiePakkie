package nl.designlama.pakkiepakkie.network.rdw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class RdwPowerParserTest {

    @Test
    fun parsePowerKw_commaDecimal() {
        assertEquals(85.5, RdwPowerParser.parsePowerKw("85,5"))
    }

    @Test
    fun parsePowerKw_dotDecimal() {
        assertEquals(100.0, RdwPowerParser.parsePowerKw("100.0"))
    }

    @Test
    fun parsePowerKw_blankReturnsNull() {
        assertNull(RdwPowerParser.parsePowerKw("   "))
        assertNull(RdwPowerParser.parsePowerKw(null))
    }

    @Test
    fun maxPowerKwFromFuelRows_picksMaximumAcrossRowsAndFields() {
        val rows = listOf(
            RdwVehicleFuelDto(nettoMaximumVermogen = "50", nominaalContinuMaximumVermogen = null),
            RdwVehicleFuelDto(nettoMaximumVermogen = "120,5", nominaalContinuMaximumVermogen = "80"),
        )
        assertEquals(120.5, RdwPowerParser.maxPowerKwFromFuelRows(rows))
    }

    @Test
    fun rdwVehicleFuelDto_decodesRdwD773NettomaximumvermogenKey() {
        val json = """[{"kenteken":"PL700K","nettomaximumvermogen":"73.60"}]"""
        val list = Json { ignoreUnknownKeys = true }.decodeFromString<List<RdwVehicleFuelDto>>(json)
        assertEquals("73.60", list.first().nettoMaximumVermogen)
        assertEquals(73.6, RdwPowerParser.maxPowerKwFromFuelRows(list))
    }

    @Test
    fun rdwVehicleFuelDto_decodesLegacyNettoMaximumVermogenKey() {
        val json = """[{"kenteken":"X","netto_maximum_vermogen":"100"}]"""
        val list = Json { ignoreUnknownKeys = true }.decodeFromString<List<RdwVehicleFuelDto>>(json)
        assertEquals("100", list.first().nettoMaximumVermogen)
    }
}

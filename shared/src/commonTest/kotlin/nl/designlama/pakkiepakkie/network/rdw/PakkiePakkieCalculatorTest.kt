package nl.designlama.pakkiepakkie.network.rdw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PakkiePakkieCalculatorTest {

    private fun car(
        kw: Double?,
        kg: Int?,
        brandstoffen: List<String> = listOf("Benzine"),
        hybrid: String? = null,
        gear: String? = "A",
    ) = VehicleLicensePlateInfo(
        kenteken = "AAAAAA",
        merk = "X",
        handelsbenaming = "Y",
        massaRijklaarKg = kg,
        massaLedigKg = null,
        cilinderinhoudCc = null,
        vermogenMassaRijklaar = null,
        vermogenKw = kw,
        brandstofOmschrijvingen = brandstoffen,
        hybridKlasse = hybrid,
        versnellingsbakCode = gear,
        aantalVersnellingen = null,
    )

    @Test
    fun winProbability_equalCarsNearFifty() {
        val c = car(100.0, 1400)
        val p = PakkiePakkieCalculator.winProbabilityPercent(c, c)
        assertTrue(p in 48f..52f)
    }

    @Test
    fun winProbability_morePowerWins() {
        val strong = car(200.0, 1400)
        val weak = car(100.0, 1400)
        val p = PakkiePakkieCalculator.winProbabilityPercent(strong, weak)
        assertTrue(p > 60f)
    }

    @Test
    fun transmissionMultiplier_manualHigherThanAutoForIce() {
        val ice = listOf("Benzine")
        val m = PakkiePakkieCalculator.transmissionMultiplier("M", ice)
        val a = PakkiePakkieCalculator.transmissionMultiplier("A", ice)
        assertTrue(m > a)
    }

    @Test
    fun transmissionMultiplier_evIgnoresGear() {
        val ev = listOf("Elektriciteit")
        assertEquals(
            PakkiePakkieCalculator.transmissionMultiplier("M", ev),
            PakkiePakkieCalculator.transmissionMultiplier("A", ev),
        )
    }
}

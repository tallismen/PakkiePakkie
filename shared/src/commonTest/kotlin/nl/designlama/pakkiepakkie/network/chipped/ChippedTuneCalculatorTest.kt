package nl.designlama.pakkiepakkie.network.chipped

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo

class ChippedTuneCalculatorTest {

    @Test
    fun estimate_benzineAddsFifteenPercent() {
        val info = sampleInfo(vermogenKw = 100.0)
        val tune = ChippedTuneCalculator.estimate(info)
        assertNotNull(tune)
        assertEquals(136, tune.stockPk)
        assertEquals(156, tune.stage1Pk)
        assertEquals(20, tune.gainPk)
    }

    @Test
    fun canBeChipped_falseForPureEv() {
        val info = sampleInfo(
            vermogenKw = 100.0,
            brandstoffen = listOf("Elektriciteit"),
        )
        assertEquals(false, ChippedTuneCalculator.canBeChipped(info))
    }

    private fun sampleInfo(
        vermogenKw: Double,
        brandstoffen: List<String> = listOf("Benzine"),
    ) = VehicleLicensePlateInfo(
        kenteken = "AAAAAA",
        merk = "X",
        handelsbenaming = "Y",
        massaRijklaarKg = 1400,
        massaLedigKg = null,
        cilinderinhoudCc = null,
        vermogenMassaRijklaar = null,
        vermogenKw = vermogenKw,
        brandstofOmschrijvingen = brandstoffen,
        hybridKlasse = null,
        versnellingsbakCode = "A",
        aantalVersnellingen = null,
        maximaleConstructiesnelheidKmh = null,
    )
}

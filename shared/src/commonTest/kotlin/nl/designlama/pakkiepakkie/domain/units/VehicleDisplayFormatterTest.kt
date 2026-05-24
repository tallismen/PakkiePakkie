package nl.designlama.pakkiepakkie.domain.units

import kotlin.test.Test
import kotlin.test.assertEquals
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo

class VehicleDisplayFormatterTest {

    private val formatter = VehicleDisplayFormatter()

    @Test
    fun formatPower_standaardShowsKwAndPk() {
        val prefs = UnitPreferences(preset = UnitPreset.Standaard, powerUnit = PowerUnit.Kw)
        assertEquals("110 kW (150 pk)", formatter.formatPower(110.0, prefs))
    }

    @Test
    fun formatPower_metricShowsKwOnly() {
        val prefs = UnitPreferences(preset = UnitPreset.Metric, powerUnit = PowerUnit.Kw)
        assertEquals("110 kW", formatter.formatPower(110.0, prefs))
    }

    @Test
    fun formatPower_imperialShowsHp() {
        val prefs = UnitPreferences(preset = UnitPreset.Imperial, powerUnit = PowerUnit.Hp)
        assertEquals("148 HP", formatter.formatPower(110.0, prefs))
    }

    @Test
    fun formatPower_pakkiePakkieShowsPakkies() {
        val prefs = UnitPreferences(preset = UnitPreset.PakkiePakkie, powerUnit = PowerUnit.Pakkies)
        assertEquals("2.8 Pakkies", formatter.formatPower(110.0, prefs))
    }

    @Test
    fun formatWeight_kg() {
        val prefs = UnitPreferences(weightUnit = WeightUnit.Kg)
        assertEquals("1420 kg", formatter.formatWeight(1420, prefs))
    }

    @Test
    fun formatWeight_lbs() {
        val prefs = UnitPreferences(weightUnit = WeightUnit.Lbs)
        assertEquals("3131 lbs", formatter.formatWeight(1420, prefs))
    }

    @Test
    fun formatWeight_slippers() {
        val prefs = UnitPreferences(weightUnit = WeightUnit.Slippers)
        assertEquals("5680 slippers", formatter.formatWeight(1420, prefs))
    }

    @Test
    fun pkPerKilo_usesRatioWhenPresent() {
        val info = sampleInfo(vermogenMassaRijklaar = 0.08)
        assertEquals(0.1088, formatter.pkPerKilo(info)!!, 0.0001)
    }

    @Test
    fun pkPerKilo_computesFromKwAndMassWhenRatioMissing() {
        val info = sampleInfo(
            vermogenMassaRijklaar = null,
            vermogenKw = 110.0,
            massaRijklaarKg = 1420,
        )
        assertEquals(0.1053, formatter.pkPerKilo(info)!!, 0.0001)
    }

    @Test
    fun formatPkPerKilo_formatsTwoDecimals() {
        val info = sampleInfo(vermogenMassaRijklaar = 0.08)
        assertEquals("0.11", formatter.formatPkPerKilo(info))
    }

    @Test
    fun presetUnits_mapsCorrectly() {
        assertEquals(PowerUnit.Kw to WeightUnit.Kg, UnitConversions.presetUnits(UnitPreset.Standaard))
        assertEquals(PowerUnit.Hp to WeightUnit.Lbs, UnitConversions.presetUnits(UnitPreset.Imperial))
        assertEquals(PowerUnit.Pakkies to WeightUnit.Slippers, UnitConversions.presetUnits(UnitPreset.PakkiePakkie))
    }

    private fun sampleInfo(
        vermogenMassaRijklaar: Double? = 0.08,
        vermogenKw: Double? = 110.0,
        massaRijklaarKg: Int? = 1420,
    ) = VehicleLicensePlateInfo(
        kenteken = "PL700K",
        merk = "VOLVO",
        handelsbenaming = "V40",
        massaRijklaarKg = massaRijklaarKg,
        massaLedigKg = 1300,
        cilinderinhoudCc = 1596,
        vermogenMassaRijklaar = vermogenMassaRijklaar,
        vermogenKw = vermogenKw,
        brandstofOmschrijvingen = listOf("Benzine"),
        hybridKlasse = null,
        versnellingsbakCode = "M",
        aantalVersnellingen = 6,
        maximaleConstructiesnelheidKmh = 210,
    )
}

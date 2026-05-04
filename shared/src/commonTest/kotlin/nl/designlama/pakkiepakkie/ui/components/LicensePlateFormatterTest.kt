package nl.designlama.pakkiepakkie.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class LicensePlateFormatterTest {

    @Test
    fun sanitize_empty_returns_empty() {
        assertEquals("", sanitizeLicensePlate(""))
    }

    @Test
    fun sanitize_uppercases_and_strips_non_alphanumerics() {
        assertEquals("PL700K", sanitizeLicensePlate(" pl-700.k! "))
        assertEquals("PL700K", sanitizeLicensePlate("pl700k"))
    }

    @Test
    fun sanitize_caps_at_six_alphanumerics() {
        assertEquals("GBB01B", sanitizeLicensePlate("GBB01BCDEFG"))
    }

    @Test
    fun format_empty_returns_empty() {
        assertEquals("", formatLicensePlate(""))
    }

    @Test
    fun format_keeps_pure_letter_or_digit_runs() {
        assertEquals("PL", formatLicensePlate("PL"))
        assertEquals("700", formatLicensePlate("700"))
    }

    @Test
    fun format_inserts_dashes_per_rdw_groups() {
        assertEquals("PL-7", formatLicensePlate("PL7"))
        assertEquals("PL-70-0", formatLicensePlate("PL700"))
        assertEquals("700-K", formatLicensePlate("700K"))
    }

    @Test
    fun format_sidecode2_four_digits_two_letters() {
        assertEquals("99-99-HF", formatLicensePlate("9999HF"))
    }

    @Test
    fun format_sidecode_examples() {
        assertEquals("PL-700-K", formatLicensePlate("PL700K"))
        assertEquals("GBB-01-B", formatLicensePlate("GBB01B"))
        assertEquals("1-KBB-00", formatLicensePlate("1KBB00"))
        assertEquals("01-GBB-1", formatLicensePlate("01GBB1"))
        assertEquals("0-GV-001", formatLicensePlate("0GV001"))
        assertEquals("456-GV-4", formatLicensePlate("456GV4"))
    }

    @Test
    fun format_after_sanitize_drops_dash_when_digits_removed() {
        assertEquals("PL", formatLicensePlate(sanitizeLicensePlate("PL-")))
    }

    @Test
    fun sanitize_dutch_plate_allows_sidecode2_four_digits_then_letters() {
        assertEquals("1234", sanitizeDutchLicensePlateInput("1234"))
        assertEquals("1234", sanitizeDutchLicensePlateInput("1234567"))
    }

    @Test
    fun sanitize_dutch_plate_allows_sidecode5_four_letters_then_digits() {
        assertEquals("AAAA", sanitizeDutchLicensePlateInput("AAAA"))
        assertEquals("AAAA", sanitizeDutchLicensePlateInput("AAAAA"))
        assertEquals("AAAA", sanitizeDutchLicensePlateInput("AAAAAA"))
    }

    @Test
    fun sanitize_dutch_plate_rejects_six_letters_or_six_digits() {
        assertEquals("AAAA", sanitizeDutchLicensePlateInput("AAAAAA"))
        assertEquals("1234", sanitizeDutchLicensePlateInput("123456"))
    }

    @Test
    fun sanitize_dutch_plate_keeps_valid_examples() {
        assertEquals("PL700K", sanitizeDutchLicensePlateInput("PL700K"))
        assertEquals("GBB01B", sanitizeDutchLicensePlateInput("GBB01B"))
        assertEquals("1KBB00", sanitizeDutchLicensePlateInput("1KBB00"))
    }

    @Test
    fun sanitize_dutch_plate_rejects_invalid_block_pattern() {
        // No sidecode matches letter–digit–letter with a 1-1-… third block like this full string.
        assertEquals("A1", sanitizeDutchLicensePlateInput("A1BCDE"))
    }
}

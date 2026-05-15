package nl.designlama.pakkiepakkie.ui.components

internal const val MAX_RAW_LENGTH = 6

/** Mask (L/D per character) and official plate segment lengths (how the RDW shows dashes). */
private data class DutchSidecodeLayout(val mask: String, val groupSizes: List<Int>)

/**
 * Sidecodes 1–14: [mask, group sizes] matching RDW display (e.g. 99-99-XX → sizes 2,2,2 on mask DDDDLL).
 */
private val DUTCH_SIDECODE_LAYOUTS: List<DutchSidecodeLayout> = listOf(
    DutchSidecodeLayout("LLDDDD", listOf(2, 2, 2)), // 1  XX-99-99
    DutchSidecodeLayout("DDDDLL", listOf(2, 2, 2)), // 2  99-99-XX
    DutchSidecodeLayout("DDLLDD", listOf(2, 2, 2)), // 3  99-XX-99
    DutchSidecodeLayout("LLDDLL", listOf(2, 2, 2)), // 4  XX-99-XX
    DutchSidecodeLayout("LLLLDD", listOf(2, 2, 2)), // 5  XX-XX-99
    DutchSidecodeLayout("DDLLLL", listOf(2, 2, 2)), // 6  99-XX-XX
    DutchSidecodeLayout("DDLLLD", listOf(2, 3, 1)), // 7  99-XXX-9
    DutchSidecodeLayout("DLLLDD", listOf(1, 3, 2)), // 8  9-XXX-99
    DutchSidecodeLayout("LLDDDL", listOf(2, 3, 1)), // 9  XX-999-X
    DutchSidecodeLayout("LDDDLL", listOf(1, 3, 2)), // 10 X-999-XX
    DutchSidecodeLayout("LLLDDL", listOf(3, 2, 1)), // 11 XXX-99-X
    DutchSidecodeLayout("LDDLLL", listOf(1, 2, 3)), // 12 X-99-XXX
    DutchSidecodeLayout("DLLDDD", listOf(1, 2, 3)), // 13 9-XX-999
    DutchSidecodeLayout("DDDLLD", listOf(3, 2, 1)), // 14 999-XX-9
)

internal val DUTCH_LICENSE_PLATE_SIDE_PATTERNS: List<String> =
    DUTCH_SIDECODE_LAYOUTS.map { it.mask }

private fun matchesDutchLicensePlatePartial(pattern: String, raw: String): Boolean {
    if (raw.length > pattern.length) return false
    for (i in raw.indices) {
        val c = raw[i]
        when (pattern[i]) {
            'L' -> if (!c.isLetter()) return false
            'D' -> if (!c.isDigit()) return false
        }
    }
    return true
}

internal fun isValidDutchLicensePlatePrefix(raw: String): Boolean =
    raw.isEmpty() || DUTCH_SIDECODE_LAYOUTS.any { matchesDutchLicensePlatePartial(it.mask, raw) }

/**
 * Picks a layout for display when several sidecodes still match the prefix. Prefers a larger
 * first segment when ambiguous (e.g. "700" → 999-XX-9 style chunk, not 99-99-XX).
 */
private fun selectFormattingLayout(raw: String): DutchSidecodeLayout? {
    val matches = DUTCH_SIDECODE_LAYOUTS.filter { matchesDutchLicensePlatePartial(it.mask, raw) }
    if (matches.isEmpty()) return null
    return matches.minWith(
        compareByDescending<DutchSidecodeLayout> { it.groupSizes[0] }
            .thenBy { DUTCH_SIDECODE_LAYOUTS.indexOf(it) },
    )
}

private fun formatPlateWithGroups(raw: String, groupSizes: List<Int>): String {
    if (raw.isEmpty()) return ""
    var pos = 0
    val parts = ArrayList<String>(3)
    for (g in groupSizes) {
        if (pos >= raw.length) break
        val end = minOf(pos + g, raw.length)
        if (end == pos) break
        parts.add(raw.substring(pos, end))
        pos = end
    }
    return parts.joinToString("-")
}

/** Inserts dashes only at letter/digit boundaries (fallback when input does not match any sidecode). */
private fun formatLicensePlateLetterDigitBoundaries(raw: String): String {
    val sb = StringBuilder(raw.length + 2)
    raw.forEachIndexed { i, c ->
        if (i > 0 && raw[i - 1].isLetter() != c.isLetter()) sb.append('-')
        sb.append(c)
    }
    return sb.toString()
}

/**
 * Formats raw plate text with dashes in RDW plate positions when the string matches at least one
 * sidecode prefix; otherwise falls back to a single dash at letter/digit transitions.
 */
fun formatLicensePlate(raw: String): String {
    val layout = selectFormattingLayout(raw) ?: return formatLicensePlateLetterDigitBoundaries(raw)
    return formatPlateWithGroups(raw, layout.groupSizes)
}

/**
 * Uppercase alphanumeric, max six characters, clipped to the longest prefix that can still become
 * a full plate under one of the RDW sidecode masks above.
 */
internal fun sanitizeDutchLicensePlateInput(input: String): String {
    val base = input.filter { it.isLetterOrDigit() }.uppercase().take(MAX_RAW_LENGTH)
    if (base.isEmpty()) return ""
    for (len in base.length downTo 1) {
        val candidate = base.take(len)
        if (isValidDutchLicensePlatePrefix(candidate)) return candidate
    }
    return ""
}

fun sanitizeLicensePlate(input: String): String =
    input.filter { it.isLetterOrDigit() }.uppercase().take(MAX_RAW_LENGTH)

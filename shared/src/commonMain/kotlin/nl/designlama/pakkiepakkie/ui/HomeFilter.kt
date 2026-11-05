package nl.designlama.pakkiepakkie.ui

import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity

enum class HomeSortBy {
    Score,
    Bouwjaar,
    Vermogen,
}

data class HomeFilter(
    val sortBy: HomeSortBy = HomeSortBy.Score,
    val scoreMin: Int? = null,
    val scoreMax: Int? = null,
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    val pkMin: Int? = null,
    val pkMax: Int? = null,
) {
    val activeCount: Int
        get() = listOfNotNull(
            sortBy.takeIf { it != HomeSortBy.Score },
            scoreMin,
            scoreMax,
            yearFrom,
            yearTo,
            pkMin,
            pkMax,
        ).size

    val isActive: Boolean get() = activeCount > 0
}

fun applyHomeFilter(
    entities: List<VehicleLookupEntity>,
    winMap: Map<String, Float?>,
    filter: HomeFilter,
): List<VehicleLookupEntity> {
    val filtered = entities.filter { row ->
        val score = winMap[row.kenteken]?.toInt()
        val pk = row.vermogenPk?.toInt()
            ?: row.vermogenKw?.let { (it * 1.36).toInt() }
        val year = row.bouwjaar
        matchesRange(score, filter.scoreMin, filter.scoreMax) &&
            matchesRange(year, filter.yearFrom, filter.yearTo) &&
            matchesRange(pk, filter.pkMin, filter.pkMax)
    }
    return when (filter.sortBy) {
        HomeSortBy.Score -> filtered.sortedByDescending { winMap[it.kenteken] ?: Float.NEGATIVE_INFINITY }
        HomeSortBy.Bouwjaar -> filtered.sortedByDescending { it.bouwjaar ?: Int.MIN_VALUE }
        HomeSortBy.Vermogen -> filtered.sortedByDescending {
            it.vermogenPk ?: it.vermogenKw?.times(1.36) ?: Double.NEGATIVE_INFINITY
        }
    }
}

private fun matchesRange(value: Int?, min: Int?, max: Int?): Boolean {
    if (min == null && max == null) return true
    if (value == null) return false
    if (min != null && value < min) return false
    if (max != null && value > max) return false
    return true
}

package nl.designlama.pakkiepakkie.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import org.koin.core.annotation.Single

@Single
class FavoritesRepository(
    private val preferencesRepository: PreferencesRepository,
) {
    private val serializer = ListSerializer(String.serializer())

    fun favoritesFlow(): Flow<List<String>> =
        preferencesRepository.getObject(
            key = PreferencesKeys.FAVORITE_KENTEKENS,
            serializer = serializer,
            defaultValue = emptyList(),
        ).map { list ->
            list.mapNotNull { raw ->
                sanitizeLicensePlate(raw).takeIf { it.length == 6 }
            }.distinct()
        }

    suspend fun isFavorite(kenteken: String): Boolean {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) return false
        return favoritesFlow().first().any { it == norm }
    }

    /**
     * @return true if the plate is now a favorite, false if removed.
     */
    suspend fun toggleFavorite(kenteken: String): Boolean {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) return false
        val current = favoritesFlow().first().toMutableList()
        val added = if (current.contains(norm)) {
            current.remove(norm)
            false
        } else {
            current.add(0, norm)
            true
        }
        preferencesRepository.saveObject(PreferencesKeys.FAVORITE_KENTEKENS, current, serializer)
        return added
    }
}

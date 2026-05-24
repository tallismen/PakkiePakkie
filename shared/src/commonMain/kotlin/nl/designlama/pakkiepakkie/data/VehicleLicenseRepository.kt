package nl.designlama.pakkiepakkie.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import nl.designlama.pakkiepakkie.data.local.VehicleDatabase
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity
import nl.designlama.pakkiepakkie.network.rdw.RdwOpenDataApi
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import org.koin.core.annotation.Single

@OptIn(ExperimentalTime::class)
@Single
class VehicleLicenseRepository(
    private val rdwOpenDataApi: RdwOpenDataApi,
    private val vehicleDatabase: VehicleDatabase,
) {
    private val dao = vehicleDatabase.vehicleLookupDao()

    fun observeRecent(limit: Int = 20): Flow<List<VehicleLookupEntity>> =
        dao.observeRecent(limit)

    suspend fun getCachedEntity(raw: String): VehicleLookupEntity? =
        withContext(Dispatchers.Default) {
            val norm = sanitizeLicensePlate(raw)
            if (norm.length != 6) null else dao.getByKenteken(norm)
        }

    /**
     * Reads Room first. Uses cache when it has enough to show; otherwise calls RDW and upserts.
     */
    suspend fun loadCachedOrRefresh(raw: String): Result<VehicleLicensePlateInfo> =
        withContext(Dispatchers.Default) {
            runCatching {
                val norm = sanitizeLicensePlate(raw)
                require(norm.length == 6) { "Kenteken moet 6 tekens zijn" }
                val cached = dao.getByKenteken(norm)
                val fullRow = cached != null && cached.dataVersion >= VehicleLookupDataVersion.FULL
                if (fullRow) {
                    val info = cached.toVehicleLicensePlateInfo()
                    if (info.hasSufficientCachedFields()) {
                        touchLastViewed(norm)
                        return@runCatching info
                    }
                }
                fetchFromApiAndPersist(norm)
            }
        }

    /** Updates sort order for recent list without fetching RDW. */
    suspend fun markRecentlyViewed(raw: String) {
        withContext(Dispatchers.Default) {
            val norm = sanitizeLicensePlate(raw)
            if (norm.length == 6) touchLastViewed(norm)
        }
    }

    /** Always hits RDW (e.g. explicit refresh later). */
    suspend fun refreshByKenteken(raw: String): Result<VehicleLicensePlateInfo> =
        withContext(Dispatchers.Default) {
            runCatching {
                val norm = sanitizeLicensePlate(raw)
                require(norm.length == 6) { "Kenteken moet 6 tekens zijn" }
                fetchFromApiAndPersist(norm)
            }
        }

    private suspend fun fetchFromApiAndPersist(normalizedKenteken: String): VehicleLicensePlateInfo {
        val info = rdwOpenDataApi.fetchByKenteken(normalizedKenteken)
        val now = Clock.System.now().toEpochMilliseconds()
        val existing = dao.getByKenteken(normalizedKenteken)
        dao.upsert(
            info.toVehicleLookupEntity(
                lastViewedAt = now,
                lastFetchedAt = now,
                dataVersion = VehicleLookupDataVersion.V3,
                isChipped = existing?.isChipped ?: false,
            ),
        )
        return info
    }

    suspend fun setChipped(raw: String, isChipped: Boolean) {
        withContext(Dispatchers.Default) {
            val norm = sanitizeLicensePlate(raw)
            if (norm.length == 6) dao.updateIsChipped(norm, isChipped)
        }
    }

    suspend fun isChipped(raw: String): Boolean =
        withContext(Dispatchers.Default) {
            val norm = sanitizeLicensePlate(raw)
            if (norm.length != 6) false else dao.getByKenteken(norm)?.isChipped ?: false
        }

    private suspend fun touchLastViewed(normalizedKenteken: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        dao.updateLastViewedAt(normalizedKenteken, now)
    }
}

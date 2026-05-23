package nl.designlama.pakkiepakkie.data.local

object VehicleLookupDataVersion {
    /** Pre–full Pakkie row (migration default). */
    const val LEGACY: Int = 1

    /** Full RDW + TGK + brandstof payload persisted. */
    const val FULL: Int = 2

    /** Includes top speed and chipped flag. */
    const val V3: Int = 3
}

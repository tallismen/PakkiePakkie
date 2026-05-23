package nl.designlama.pakkiepakkie.data.local

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

@DeleteTable(tableName = "atm_tune_cache")
class DeleteAtmTuneCacheMigration : AutoMigrationSpec

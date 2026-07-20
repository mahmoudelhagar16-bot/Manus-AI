package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import com.farm.layermanager.data.local.entity.SchemaMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchemaMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SchemaMetadataEntity)

    @Query("SELECT * FROM schema_metadata WHERE id = 1")
    suspend fun getOnce(): SchemaMetadataEntity?

    @Query("SELECT * FROM schema_metadata WHERE id = 1")
    fun get(): Flow<SchemaMetadataEntity?>

    @Query("UPDATE schema_metadata SET lastBackupDate = :date WHERE id = 1")
    suspend fun updateLastBackupDate(date: String)
}

package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.farm.layermanager.data.local.entity.StrainCumulativeStatsView
import com.farm.layermanager.data.local.entity.StrainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrainDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(strain: StrainEntity): Long

    @Update
    suspend fun update(strain: StrainEntity)

    @Query("UPDATE strains SET status = :status WHERE strainId = :strainId")
    suspend fun updateStatus(strainId: Long, status: String)

    @Query("SELECT * FROM strains WHERE houseId = :houseId ORDER BY arrivalDate DESC")
    fun getByHouse(houseId: Long): Flow<List<StrainEntity>>

    @Query("SELECT * FROM strains WHERE status = 'ACTIVE' ORDER BY arrivalDate DESC")
    fun getActive(): Flow<List<StrainEntity>>

    @Query("SELECT * FROM strains ORDER BY arrivalDate DESC")
    fun getAll(): Flow<List<StrainEntity>>

    @Query("SELECT * FROM strains WHERE strainId = :strainId")
    fun getById(strainId: Long): Flow<StrainEntity?>

    @Query("SELECT * FROM strain_cumulative_stats WHERE strainId = :strainId")
    fun getCumulativeStats(strainId: Long): Flow<StrainCumulativeStatsView?>

    @Query("SELECT * FROM strain_cumulative_stats")
    fun getAllCumulativeStats(): Flow<List<StrainCumulativeStatsView>>
}

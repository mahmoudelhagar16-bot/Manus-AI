package com.farm.layermanager.domain.repository

import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.Strain
import kotlinx.coroutines.flow.Flow

/** يقابل View: strain_cumulative_stats (قسم 12/14) — لا تُخزَّن هذه القيم، تُشتق دائماً. */
data class StrainCumulativeStats(
    val strainId: Long,
    val initialChickCount: Int,
    val totalMortality: Int,
    val totalCulled: Int,
    val currentBirds: Int
)

interface HouseRepository {
    suspend fun insert(house: House): Long
    suspend fun update(house: House)
    suspend fun setStatus(houseId: Long, status: String)
    fun getAll(): Flow<List<House>>
    fun getActive(): Flow<List<House>>
    fun getById(houseId: Long): Flow<House?>
    suspend fun hasRelatedDailyRecords(houseId: Long): Boolean
}

interface StrainRepository {
    suspend fun insert(strain: Strain): Long
    suspend fun update(strain: Strain)
    suspend fun setStatus(strainId: Long, status: String)
    fun getByHouse(houseId: Long): Flow<List<Strain>>
    fun getActive(): Flow<List<Strain>>
    fun getAll(): Flow<List<Strain>>
    fun getById(strainId: Long): Flow<Strain?>
    fun getCumulativeStats(strainId: Long): Flow<StrainCumulativeStats?>
    fun getAllCumulativeStats(): Flow<List<StrainCumulativeStats>>
}

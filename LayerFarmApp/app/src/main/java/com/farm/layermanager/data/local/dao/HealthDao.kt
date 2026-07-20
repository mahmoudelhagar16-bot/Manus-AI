package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.farm.layermanager.data.local.entity.MedicationEntity
import com.farm.layermanager.data.local.entity.VaccinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Insert
    suspend fun insert(vaccination: VaccinationEntity): Long

    @Update
    suspend fun update(vaccination: VaccinationEntity)

    @Query("SELECT * FROM vaccinations WHERE strainId = :strainId ORDER BY vDate DESC")
    fun getByStrain(strainId: Long): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations ORDER BY vDate DESC")
    fun getAll(): Flow<List<VaccinationEntity>>
}

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Query("SELECT * FROM medications WHERE strainId = :strainId ORDER BY mDate DESC")
    fun getByStrain(strainId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY mDate DESC")
    fun getAll(): Flow<List<MedicationEntity>>

    @Query("SELECT IFNULL(SUM(cost), 0) FROM medications WHERE strainId = :strainId AND mDate BETWEEN :startDate AND :endDate")
    fun getTotalCostForStrainInRange(strainId: Long, startDate: Long, endDate: Long): Flow<Double>
}

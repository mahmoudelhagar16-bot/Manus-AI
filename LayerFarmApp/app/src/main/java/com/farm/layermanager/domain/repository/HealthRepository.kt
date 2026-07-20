package com.farm.layermanager.domain.repository

import com.farm.layermanager.domain.model.Medication
import com.farm.layermanager.domain.model.Vaccination
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface VaccinationRepository {
    suspend fun insert(vaccination: Vaccination): Long
    suspend fun update(vaccination: Vaccination)
    fun getByStrain(strainId: Long): Flow<List<Vaccination>>
    fun getAll(): Flow<List<Vaccination>>
}

interface MedicationRepository {
    suspend fun insert(medication: Medication): Long
    suspend fun update(medication: Medication)
    fun getByStrain(strainId: Long): Flow<List<Medication>>
    fun getAll(): Flow<List<Medication>>
    fun getTotalCostForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double>
}

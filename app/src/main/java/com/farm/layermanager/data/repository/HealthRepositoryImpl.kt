package com.farm.layermanager.data.repository

import com.farm.layermanager.data.local.dao.MedicationDao
import com.farm.layermanager.data.local.dao.VaccinationDao
import com.farm.layermanager.data.mapper.toDomain
import com.farm.layermanager.data.mapper.toEntity
import com.farm.layermanager.domain.model.Medication
import com.farm.layermanager.domain.model.Vaccination
import com.farm.layermanager.domain.repository.MedicationRepository
import com.farm.layermanager.domain.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class VaccinationRepositoryImpl @Inject constructor(
    private val dao: VaccinationDao
) : VaccinationRepository {

    override suspend fun insert(vaccination: Vaccination): Long = dao.insert(vaccination.toEntity())

    override suspend fun update(vaccination: Vaccination) = dao.update(vaccination.toEntity())

    override fun getByStrain(strainId: Long): Flow<List<Vaccination>> =
        dao.getByStrain(strainId).map { list -> list.map { it.toDomain() } }

    override fun getAll(): Flow<List<Vaccination>> = dao.getAll().map { list -> list.map { it.toDomain() } }
}

class MedicationRepositoryImpl @Inject constructor(
    private val dao: MedicationDao
) : MedicationRepository {

    override suspend fun insert(medication: Medication): Long = dao.insert(medication.toEntity())

    override suspend fun update(medication: Medication) = dao.update(medication.toEntity())

    override fun getByStrain(strainId: Long): Flow<List<Medication>> =
        dao.getByStrain(strainId).map { list -> list.map { it.toDomain() } }

    override fun getAll(): Flow<List<Medication>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getTotalCostForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        dao.getTotalCostForStrainInRange(strainId, startDate.toEpochDay(), endDate.toEpochDay())
}

package com.farm.layermanager.domain.usecase.health

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.common.Validator
import com.farm.layermanager.domain.model.Medication
import com.farm.layermanager.domain.model.Vaccination
import com.farm.layermanager.domain.repository.MedicationRepository
import com.farm.layermanager.domain.repository.VaccinationRepository
import kotlinx.coroutines.flow.Flow

class AddVaccinationUseCase(private val repository: VaccinationRepository) {
    suspend operator fun invoke(vaccination: Vaccination): DomainResult<Long> {
        Validator.requireNotBlank(vaccination.vaccineName, "اسم التحصين")?.let { return DomainResult.Error(it) }

        val id = repository.insert(vaccination)
        return DomainResult.Success(id)
    }
}

class GetVaccinationsUseCase(private val repository: VaccinationRepository) {
    fun getByStrain(strainId: Long): Flow<List<Vaccination>> = repository.getByStrain(strainId)
    fun getAll(): Flow<List<Vaccination>> = repository.getAll()
}

class AddMedicationUseCase(private val repository: MedicationRepository) {
    suspend operator fun invoke(medication: Medication): DomainResult<Long> {
        Validator.requireNotBlank(medication.medicineName, "اسم الدواء")?.let { return DomainResult.Error(it) }
        Validator.requireNonNegative(medication.cost, "التكلفة")?.let { return DomainResult.Error(it) }

        val id = repository.insert(medication)
        return DomainResult.Success(id)
    }
}

class GetMedicationsUseCase(private val repository: MedicationRepository) {
    fun getByStrain(strainId: Long): Flow<List<Medication>> = repository.getByStrain(strainId)
    fun getAll(): Flow<List<Medication>> = repository.getAll()
}

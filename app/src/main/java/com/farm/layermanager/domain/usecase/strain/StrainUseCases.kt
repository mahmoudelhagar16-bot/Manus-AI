package com.farm.layermanager.domain.usecase.strain

import com.farm.layermanager.domain.calculation.CalculationEngine
import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.common.Validator
import com.farm.layermanager.domain.model.EggColor
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.model.StrainStats
import com.farm.layermanager.domain.model.StrainStatus
import com.farm.layermanager.domain.repository.StrainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class AddStrainUseCase(private val repository: StrainRepository) {
    suspend operator fun invoke(strain: Strain): DomainResult<Long> {
        Validator.requireNotBlank(strain.strainName, "اسم السلالة")?.let { return DomainResult.Error(it) }
        Validator.requirePositive(strain.initialChickCount, "عدد الكتاكيت الابتدائي")?.let { return DomainResult.Error(it) }
        if (strain.eggColor !in EggColor.values()) {
            return DomainResult.Error("لون البيض يجب أن يكون أبيض أو أحمر")
        }
        if (strain.arrivalDate.isAfter(LocalDate.now())) {
            return DomainResult.Error("تاريخ الاستقبال لا يمكن أن يكون في المستقبل")
        }

        val id = repository.insert(strain)
        return DomainResult.Success(id)
    }
}

class UpdateStrainUseCase(private val repository: StrainRepository) {
    suspend operator fun invoke(strain: Strain): DomainResult<Unit> {
        Validator.requireNotBlank(strain.strainName, "اسم السلالة")?.let { return DomainResult.Error(it) }
        Validator.requirePositive(strain.initialChickCount, "عدد الكتاكيت الابتدائي")?.let { return DomainResult.Error(it) }

        repository.update(strain)
        return DomainResult.Success(Unit)
    }
}

class DeactivateStrainUseCase(private val repository: StrainRepository) {
    suspend operator fun invoke(strainId: Long): DomainResult<Unit> {
        repository.setStatus(strainId, StrainStatus.INACTIVE.name)
        return DomainResult.Success(Unit)
    }
}

/**
 * UC-03: يبني StrainStats كاملة (عمر، بقاء، نافق تراكمي...) عند الطلب فقط، بدون أي تخزين وسيط.
 */
class GetStrainDetailsUseCase(private val repository: StrainRepository) {

    fun getByHouse(houseId: Long): Flow<List<Strain>> = repository.getByHouse(houseId)

    fun getStats(strainId: Long): Flow<StrainStats?> =
        repository.getById(strainId).combine(repository.getCumulativeStats(strainId)) { strain, stats ->
            if (strain == null || stats == null) return@combine null
            buildStats(strain, stats.totalMortality, stats.totalCulled)
        }

    fun getAllStats(): Flow<List<StrainStats>> =
        repository.getAll().combine(repository.getAllCumulativeStats()) { strains, statsList ->
            val statsById = statsList.associateBy { it.strainId }
            strains.mapNotNull { strain ->
                val stats = statsById[strain.strainId] ?: return@mapNotNull null
                buildStats(strain, stats.totalMortality, stats.totalCulled)
            }
        }

    private fun buildStats(strain: Strain, totalMortality: Int, totalCulled: Int): StrainStats {
        val currentBirds = CalculationEngine.currentBirdCount(strain.initialChickCount, totalMortality, totalCulled)
        return StrainStats(
            strain = strain,
            ageInWeeks = CalculationEngine.currentAgeWeeks(strain.arrivalDate),
            cumulativeMortality = totalMortality,
            cumulativeCulled = totalCulled,
            currentBirdCount = currentBirds,
            livabilityPercent = CalculationEngine.livabilityPercent(currentBirds, strain.initialChickCount),
            mortalityPercent = CalculationEngine.mortalityPercent(totalMortality, strain.initialChickCount)
        )
    }
}

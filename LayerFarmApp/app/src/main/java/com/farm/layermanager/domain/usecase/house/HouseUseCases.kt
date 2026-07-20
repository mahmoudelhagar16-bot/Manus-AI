package com.farm.layermanager.domain.usecase.house

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.common.Validator
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.HouseStatus
import com.farm.layermanager.domain.repository.HouseRepository
import kotlinx.coroutines.flow.Flow

class AddHouseUseCase(private val repository: HouseRepository) {
    suspend operator fun invoke(name: String, number: String, notes: String?): DomainResult<Long> {
        Validator.requireNotBlank(name, "الاسم")?.let { return DomainResult.Error(it) }
        Validator.requireNotBlank(number, "الرقم")?.let { return DomainResult.Error(it) }

        val id = repository.insert(House(name = name, number = number, notes = notes))
        return DomainResult.Success(id)
    }
}

class UpdateHouseUseCase(private val repository: HouseRepository) {
    suspend operator fun invoke(house: House): DomainResult<Unit> {
        Validator.requireNotBlank(house.name, "الاسم")?.let { return DomainResult.Error(it) }
        Validator.requireNotBlank(house.number, "الرقم")?.let { return DomainResult.Error(it) }

        repository.update(house)
        return DomainResult.Success(Unit)
    }
}

/**
 * تعطيل عنبر (تغيير الحالة إلى INACTIVE) — الحذف الفعلي ممنوع دائماً (RESTRICT في قاعدة البيانات، قسم 14).
 * لا حاجة للتحقق من السجلات المرتبطة هنا لأن التعطيل لا يحذف شيئاً، لكنه متاح كتنبيه اختياري للواجهة.
 */
class DeactivateHouseUseCase(private val repository: HouseRepository) {
    suspend operator fun invoke(houseId: Long): DomainResult<Unit> {
        repository.setStatus(houseId, HouseStatus.INACTIVE.name)
        return DomainResult.Success(Unit)
    }
}

class ReactivateHouseUseCase(private val repository: HouseRepository) {
    suspend operator fun invoke(houseId: Long): DomainResult<Unit> {
        repository.setStatus(houseId, HouseStatus.ACTIVE.name)
        return DomainResult.Success(Unit)
    }
}

class GetHousesUseCase(private val repository: HouseRepository) {
    fun getAll(): Flow<List<House>> = repository.getAll()
    fun getActive(): Flow<List<House>> = repository.getActive()
    fun getById(houseId: Long): Flow<House?> = repository.getById(houseId)
}

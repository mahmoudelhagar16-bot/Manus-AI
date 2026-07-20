package com.farm.layermanager.domain.usecase.dailyrecord

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.model.DailyRecord
import com.farm.layermanager.domain.repository.DailyRecordRepository
import com.farm.layermanager.domain.repository.StrainRepository
import com.farm.layermanager.domain.validation.DailyRecordValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * UC-04 + UC-05: إدخال السجل اليومي مع التحقق الكامل قبل الحفظ.
 * يرفض الإدراج إن وُجد سجل سابق لنفس (recordDate, houseId, strainId) — بدلاً من الاعتماد فقط
 * على استثناء الـ UNIQUE constraint في قاعدة البيانات، حتى تصل رسالة واضحة للواجهة.
 */
class AddDailyRecordUseCase(
    private val dailyRecordRepository: DailyRecordRepository,
    private val strainRepository: StrainRepository
) {
    suspend operator fun invoke(record: DailyRecord): DomainResult<Long> {
        val existing = dailyRecordRepository.findByDateHouseStrain(record.recordDate, record.houseId, record.strainId)
        if (existing != null) {
            return DomainResult.Error("يوجد سجل مسجَّل بالفعل لهذا اليوم لنفس العنبر/السلالة. استخدم التعديل بدلاً من الإضافة")
        }

        val strain = strainRepository.getById(record.strainId).first()
            ?: return DomainResult.Error("السلالة غير موجودة")

        val previous = dailyRecordRepository.getPreviousRecord(record.houseId, record.strainId, record.recordDate)

        val errors = DailyRecordValidator.validate(
            record = record,
            previousLiveBirds = previous?.liveBirds,
            referenceBirdsForFirstRecord = strain.initialChickCount
        )
        if (errors.isNotEmpty()) {
            return DomainResult.Error(errors.joinToString(separator = "\n"))
        }

        val id = dailyRecordRepository.insert(record)
        return DomainResult.Success(id)
    }
}

/**
 * UC-06: تعديل سجل سابق بأثر رجعي.
 * مسموح دائماً لأن كل الحسابات التراكمية (نافق/بقاء/مخزون) تُشتق من daily_records ولا تُخزَّن،
 * لذا لا يوجد خطر "كسر تسلسل" فعلي (قسم 14) — لكن يجب إعادة التحقق مقابل السجل السابق لنفس التاريخ.
 */
class UpdateDailyRecordUseCase(
    private val dailyRecordRepository: DailyRecordRepository,
    private val strainRepository: StrainRepository
) {
    suspend operator fun invoke(record: DailyRecord): DomainResult<Unit> {
        val strain = strainRepository.getById(record.strainId).first()
            ?: return DomainResult.Error("السلالة غير موجودة")

        val previous = dailyRecordRepository.getPreviousRecord(record.houseId, record.strainId, record.recordDate)

        val errors = DailyRecordValidator.validate(
            record = record,
            previousLiveBirds = previous?.liveBirds,
            referenceBirdsForFirstRecord = strain.initialChickCount
        )
        if (errors.isNotEmpty()) {
            return DomainResult.Error(errors.joinToString(separator = "\n"))
        }

        dailyRecordRepository.update(record)
        return DomainResult.Success(Unit)
    }
}

class DeleteDailyRecordUseCase(private val repository: DailyRecordRepository) {
    suspend operator fun invoke(recordId: Long): DomainResult<Unit> {
        repository.delete(recordId)
        return DomainResult.Success(Unit)
    }
}

class GetDailyRecordsUseCase(private val repository: DailyRecordRepository) {
    fun getByStrain(houseId: Long, strainId: Long): Flow<List<DailyRecord>> =
        repository.getByStrain(houseId, strainId)

    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyRecord>> =
        repository.getByDateRange(startDate, endDate)

    fun getByStrainAndDateRange(houseId: Long, strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyRecord>> =
        repository.getByStrainAndDateRange(houseId, strainId, startDate, endDate)
}

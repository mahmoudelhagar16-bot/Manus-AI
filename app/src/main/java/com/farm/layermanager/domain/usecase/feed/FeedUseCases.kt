package com.farm.layermanager.domain.usecase.feed

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.common.Validator
import com.farm.layermanager.domain.model.FeedConsumption
import com.farm.layermanager.domain.model.FeedType
import com.farm.layermanager.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** UC-09 (جزء 1): إضافة نوع علف جديد لأول مرة (وارد أول). */
class AddFeedTypeUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(feedType: FeedType): DomainResult<Long> {
        Validator.requireNotBlank(feedType.feedName, "اسم العلف")?.let { return DomainResult.Error(it) }
        Validator.requirePositive(feedType.bagWeightKg, "وزن الشيكارة")?.let { return DomainResult.Error(it) }
        Validator.requireNonNegative(feedType.price, "السعر")?.let { return DomainResult.Error(it) }
        Validator.requireNonNegative(feedType.quantityKg, "الكمية")?.let { return DomainResult.Error(it) }

        val id = repository.insertFeedType(feedType.copy(currentStockKg = feedType.quantityKg, weightedAvgCost = feedType.price))
        return DomainResult.Success(id)
    }
}

/** UC-09 (جزء 2): وارد إضافي لنوع علف موجود — يُحدِّث المتوسط المرجّح للتكلفة تلقائياً (قسم 5.5). */
class AddFeedPurchaseUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(feedTypeId: Long, newQtyKg: Double, newPrice: Double): DomainResult<Unit> {
        Validator.requirePositive(newQtyKg, "الكمية")?.let { return DomainResult.Error(it) }
        Validator.requireNonNegative(newPrice, "السعر")?.let { return DomainResult.Error(it) }

        return repository.recordFeedPurchase(feedTypeId, newQtyKg, newPrice)
    }
}

/**
 * UC-10: تسجيل استهلاك علف يومي — خصم تلقائي من المخزون + منع الصرف الأكبر من الرصيد.
 * التحقق الفعلي من الرصيد يتم داخل الـ Repository ضمن نفس الـ Transaction (خط دفاع أخير)،
 * لكن نتحقق هنا أيضاً بشكل مبكر لإعطاء رسالة فورية للواجهة دون الحاجة لضرب قاعدة البيانات.
 */
class RecordFeedConsumptionUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(consumption: FeedConsumption): DomainResult<Long> {
        Validator.requirePositive(consumption.totalWeightKg, "الكمية المستهلكة")?.let { return DomainResult.Error(it) }

        val feedType = repository.getFeedTypeByIdOnce(consumption.feedTypeId)
            ?: return DomainResult.Error("نوع العلف غير موجود")

        if (consumption.totalWeightKg > feedType.currentStockKg) {
            return DomainResult.Error(
                "الكمية المطلوب صرفها (${consumption.totalWeightKg} كجم) أكبر من الرصيد المتاح (${feedType.currentStockKg} كجم)"
            )
        }

        return repository.recordConsumption(consumption)
    }
}

class GetFeedUseCase(private val repository: FeedRepository) {
    fun getFeedTypes(): Flow<List<FeedType>> = repository.getFeedTypes()
    fun getConsumptionByStrain(houseId: Long, strainId: Long): Flow<List<FeedConsumption>> =
        repository.getConsumptionByStrain(houseId, strainId)
    fun getTotalWeightForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        repository.getConsumptionTotalWeightForStrainInRange(strainId, startDate, endDate)
    fun getTotalCostForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        repository.getConsumptionTotalCostForStrainInRange(strainId, startDate, endDate)
}

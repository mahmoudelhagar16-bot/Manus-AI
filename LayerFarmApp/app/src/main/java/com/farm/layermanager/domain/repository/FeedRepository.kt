package com.farm.layermanager.domain.repository

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.model.FeedConsumption
import com.farm.layermanager.domain.model.FeedType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FeedRepository {
    suspend fun insertFeedType(feedType: FeedType): Long
    fun getFeedTypes(): Flow<List<FeedType>>
    fun getFeedTypeById(feedTypeId: Long): Flow<FeedType?>
    suspend fun getFeedTypeByIdOnce(feedTypeId: Long): FeedType?

    /** يُحدِّث المتوسط المرجّح للتكلفة والرصيد ضمن Transaction واحدة (قسم 5.5). */
    suspend fun recordFeedPurchase(feedTypeId: Long, newQtyKg: Double, newPrice: Double): DomainResult<Unit>

    /** يخصم من المخزون ويسجّل الاستهلاك ضمن Transaction واحدة، ويرفض إن تجاوزت totalWeightKg الرصيد (UC-10). */
    suspend fun recordConsumption(consumption: FeedConsumption): DomainResult<Long>

    fun getConsumptionByStrain(houseId: Long, strainId: Long): Flow<List<FeedConsumption>>
    fun getConsumptionTotalWeightForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double>
    fun getConsumptionTotalCostForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double>
}

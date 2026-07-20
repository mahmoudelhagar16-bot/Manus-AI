package com.farm.layermanager.data.repository

import com.farm.layermanager.data.local.dao.FeedConsumptionDao
import com.farm.layermanager.data.local.dao.FeedConsumptionTransactionDao
import com.farm.layermanager.data.local.dao.FeedTypeDao
import com.farm.layermanager.data.mapper.toDomain
import com.farm.layermanager.data.mapper.toEntity
import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.model.FeedConsumption
import com.farm.layermanager.domain.model.FeedType
import com.farm.layermanager.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val feedTypeDao: FeedTypeDao,
    private val feedConsumptionDao: FeedConsumptionDao,
    private val transactionDao: FeedConsumptionTransactionDao
) : FeedRepository {

    override suspend fun insertFeedType(feedType: FeedType): Long = feedTypeDao.insert(feedType.toEntity())

    override fun getFeedTypes(): Flow<List<FeedType>> = feedTypeDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getFeedTypeById(feedTypeId: Long): Flow<FeedType?> = feedTypeDao.getById(feedTypeId).map { it?.toDomain() }

    override suspend fun getFeedTypeByIdOnce(feedTypeId: Long): FeedType? = feedTypeDao.getByIdOnce(feedTypeId)?.toDomain()

    override suspend fun recordFeedPurchase(feedTypeId: Long, newQtyKg: Double, newPrice: Double): DomainResult<Unit> {
        val result = transactionDao.recordFeedPurchase(feedTypeId, newQtyKg, newPrice)
        return result.fold(
            onSuccess = { DomainResult.Success(Unit) },
            onFailure = { DomainResult.Error(it.message ?: "فشل تسجيل وارد العلف") }
        )
    }

    override suspend fun recordConsumption(consumption: FeedConsumption): DomainResult<Long> {
        val result = transactionDao.recordConsumption(consumption.toEntity())
        return result.fold(
            onSuccess = { id -> DomainResult.Success(id) },
            onFailure = { DomainResult.Error(it.message ?: "فشل تسجيل استهلاك العلف") }
        )
    }

    override fun getConsumptionByStrain(houseId: Long, strainId: Long): Flow<List<FeedConsumption>> =
        feedConsumptionDao.getByStrain(houseId, strainId).map { list -> list.map { it.toDomain() } }

    override fun getConsumptionTotalWeightForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        feedConsumptionDao.getTotalWeightForStrainInRange(strainId, startDate.toEpochDay(), endDate.toEpochDay())

    override fun getConsumptionTotalCostForStrainInRange(strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<Double> =
        feedConsumptionDao.getTotalCostForStrainInRange(strainId, startDate.toEpochDay(), endDate.toEpochDay())
}

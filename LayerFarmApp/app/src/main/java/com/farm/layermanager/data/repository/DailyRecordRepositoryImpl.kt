package com.farm.layermanager.data.repository

import com.farm.layermanager.data.local.dao.DailyRecordDao
import com.farm.layermanager.data.mapper.toDomain
import com.farm.layermanager.data.mapper.toEntity
import com.farm.layermanager.domain.model.DailyRecord
import com.farm.layermanager.domain.repository.DailyRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class DailyRecordRepositoryImpl @Inject constructor(
    private val dao: DailyRecordDao
) : DailyRecordRepository {

    override suspend fun insert(record: DailyRecord): Long = dao.insert(record.toEntity())

    override suspend fun update(record: DailyRecord) = dao.update(record.toEntity())

    override suspend fun delete(recordId: Long) = dao.delete(recordId)

    override suspend fun findByDateHouseStrain(date: LocalDate, houseId: Long, strainId: Long): DailyRecord? =
        dao.findByDateHouseStrain(date.toEpochDay(), houseId, strainId)?.toDomain()

    override suspend fun getPreviousRecord(houseId: Long, strainId: Long, date: LocalDate): DailyRecord? =
        dao.getPreviousRecord(houseId, strainId, date.toEpochDay())?.toDomain()

    override fun getByStrain(houseId: Long, strainId: Long): Flow<List<DailyRecord>> =
        dao.getByStrain(houseId, strainId).map { list -> list.map { it.toDomain() } }

    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyRecord>> =
        dao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun getByStrainAndDateRange(
        houseId: Long,
        strainId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DailyRecord>> =
        dao.getByStrainAndDateRange(houseId, strainId, startDate.toEpochDay(), endDate.toEpochDay())
            .map { list -> list.map { it.toDomain() } }
}

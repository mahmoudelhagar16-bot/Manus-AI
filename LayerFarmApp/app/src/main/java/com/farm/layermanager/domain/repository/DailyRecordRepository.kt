package com.farm.layermanager.domain.repository

import com.farm.layermanager.domain.model.DailyRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyRecordRepository {
    suspend fun insert(record: DailyRecord): Long
    suspend fun update(record: DailyRecord)
    suspend fun delete(recordId: Long)
    suspend fun findByDateHouseStrain(date: LocalDate, houseId: Long, strainId: Long): DailyRecord?
    suspend fun getPreviousRecord(houseId: Long, strainId: Long, date: LocalDate): DailyRecord?
    fun getByStrain(houseId: Long, strainId: Long): Flow<List<DailyRecord>>
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyRecord>>
    fun getByStrainAndDateRange(houseId: Long, strainId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyRecord>>
}

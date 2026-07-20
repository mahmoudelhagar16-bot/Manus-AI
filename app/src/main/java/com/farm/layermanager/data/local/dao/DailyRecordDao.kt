package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.farm.layermanager.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {

    /** ABORT بحيث يفشل الإدراج فوراً لو خالف الـ UNIQUE(recordDate, houseId, strainId) */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: DailyRecordEntity): Long

    @Update
    suspend fun update(record: DailyRecordEntity)

    @Query("DELETE FROM daily_records WHERE recordId = :recordId")
    suspend fun delete(recordId: Long)

    @Query(
        """
        SELECT * FROM daily_records
        WHERE houseId = :houseId AND strainId = :strainId
        ORDER BY recordDate DESC
        """
    )
    fun getByStrain(houseId: Long, strainId: Long): Flow<List<DailyRecordEntity>>

    @Query(
        """
        SELECT * FROM daily_records
        WHERE recordDate = :date AND houseId = :houseId AND strainId = :strainId
        LIMIT 1
        """
    )
    suspend fun findByDateHouseStrain(date: Long, houseId: Long, strainId: Long): DailyRecordEntity?

    /** آخر سجل قبل تاريخ معيّن — يُستخدم في التحقق (mortality+culled <= liveBirds اليوم السابق) */
    @Query(
        """
        SELECT * FROM daily_records
        WHERE houseId = :houseId AND strainId = :strainId AND recordDate < :date
        ORDER BY recordDate DESC
        LIMIT 1
        """
    )
    suspend fun getPreviousRecord(houseId: Long, strainId: Long, date: Long): DailyRecordEntity?

    @Query(
        """
        SELECT * FROM daily_records
        WHERE recordDate BETWEEN :startDate AND :endDate
        ORDER BY recordDate DESC
        """
    )
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<DailyRecordEntity>>

    @Query(
        """
        SELECT * FROM daily_records
        WHERE houseId = :houseId AND strainId = :strainId
              AND recordDate BETWEEN :startDate AND :endDate
        ORDER BY recordDate ASC
        """
    )
    fun getByStrainAndDateRange(houseId: Long, strainId: Long, startDate: Long, endDate: Long): Flow<List<DailyRecordEntity>>

    @Query("SELECT IFNULL(SUM(mortality), 0) FROM daily_records WHERE recordDate = :date")
    fun getTotalMortalityForDate(date: Long): Flow<Int>

    @Query("SELECT IFNULL(SUM(productionTrays), 0) FROM daily_records WHERE recordDate = :date")
    fun getTotalProductionForDate(date: Long): Flow<Double>
}

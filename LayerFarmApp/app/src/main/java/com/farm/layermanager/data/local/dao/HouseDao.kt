package com.farm.layermanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.farm.layermanager.data.local.entity.HouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(house: HouseEntity): Long

    @Update
    suspend fun update(house: HouseEntity)

    @Query("UPDATE houses SET status = :status WHERE houseId = :houseId")
    suspend fun updateStatus(houseId: Long, status: String)

    @Query("SELECT * FROM houses ORDER BY name ASC")
    fun getAll(): Flow<List<HouseEntity>>

    @Query("SELECT * FROM houses WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getActive(): Flow<List<HouseEntity>>

    @Query("SELECT * FROM houses WHERE houseId = :houseId")
    fun getById(houseId: Long): Flow<HouseEntity?>

    @Query("SELECT COUNT(*) FROM houses WHERE status = 'ACTIVE'")
    fun getActiveCount(): Flow<Int>

    /** يُستخدم قبل محاولة تعطيل عنبر للتحقق من عدم وجود سجلات مرتبطة إن رغبنا بمنع حتى التعطيل بشرط معين مستقبلاً */
    @Query("SELECT COUNT(*) FROM daily_records WHERE houseId = :houseId")
    suspend fun countRelatedDailyRecords(houseId: Long): Int
}

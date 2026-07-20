package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * جدول السجل اليومي (daily_records)
 * القيد الفريد (recordDate, houseId, strainId) يمنع تكرار سجل نفس اليوم لنفس (عنبر × سلالة).
 * كل قواعد الـ Validation الرقمية (mortality+culled <= liveBirds اليوم السابق ... إلخ)
 * تُنفَّذ في طبقة الـ Domain (AddDailyRecordUseCase) قبل الإدراج، وليس هنا.
 */
@Entity(
    tableName = "daily_records",
    foreignKeys = [
        ForeignKey(
            entity = HouseEntity::class,
            parentColumns = ["houseId"],
            childColumns = ["houseId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = StrainEntity::class,
            parentColumns = ["strainId"],
            childColumns = ["strainId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["houseId", "strainId", "recordDate"], unique = true, name = "idx_daily_unique"),
        Index(value = ["recordDate"]),
        Index(value = ["strainId"])
    ]
)
data class DailyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "recordId")
    val recordId: Long = 0,

    @ColumnInfo(name = "recordDate")
    val recordDate: LocalDate,

    @ColumnInfo(name = "houseId")
    val houseId: Long,

    @ColumnInfo(name = "strainId")
    val strainId: Long,

    @ColumnInfo(name = "liveBirds")
    val liveBirds: Int,

    @ColumnInfo(name = "mortality")
    val mortality: Int,

    @ColumnInfo(name = "culled")
    val culled: Int,

    @ColumnInfo(name = "feedQtyKg")
    val feedQtyKg: Double,

    @ColumnInfo(name = "waterLiters")
    val waterLiters: Double,

    @ColumnInfo(name = "temperature")
    val temperature: Double? = null,

    @ColumnInfo(name = "humidity")
    val humidity: Double? = null,

    @ColumnInfo(name = "lightHours")
    val lightHours: Double? = null,

    @ColumnInfo(name = "productionTrays")
    val productionTrays: Double,

    @ColumnInfo(name = "crackedEggs")
    val crackedEggs: Int = 0,

    @ColumnInfo(name = "deformedEggs")
    val deformedEggs: Int = 0,

    @ColumnInfo(name = "floorEggs")
    val floorEggs: Int = 0,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * أنواع العلف. currentStockKg و weightedAvgCost يُحدَّثان ضمن نفس الـ Transaction
 * عند كل عملية وارد جديدة (راجع UseCase: AddFeedPurchaseUseCase).
 * weightedAvgCost هو الاستثناء الوحيد لقيمة "محسوبة" تُخزَّن، لأنها Historical Frozen Cost.
 */
@Entity(
    tableName = "feed_types",
    indices = [Index(value = ["feedName"])]
)
data class FeedTypeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "feedTypeId")
    val feedTypeId: Long = 0,

    @ColumnInfo(name = "feedName")
    val feedName: String,

    @ColumnInfo(name = "company")
    val company: String? = null,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "bagWeightKg")
    val bagWeightKg: Double,

    @ColumnInfo(name = "purchaseDate")
    val purchaseDate: LocalDate,

    @ColumnInfo(name = "quantityKg")
    val quantityKg: Double,

    @ColumnInfo(name = "currentStockKg")
    val currentStockKg: Double,

    /** متوسط التكلفة المرجّح — الحقل الوحيد المسموح تخزينه كقيمة "محسوبة" */
    @ColumnInfo(name = "weightedAvgCost")
    val weightedAvgCost: Double = price
)

/**
 * استهلاك العلف اليومي لكل (عنبر × سلالة × نوع علف).
 * الإدراج هنا يجب أن يتم ضمن @Transaction واحدة مع خصم currentStockKg من feed_types.
 */
@Entity(
    tableName = "feed_consumption",
    foreignKeys = [
        ForeignKey(entity = HouseEntity::class, parentColumns = ["houseId"], childColumns = ["houseId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = StrainEntity::class, parentColumns = ["strainId"], childColumns = ["strainId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = FeedTypeEntity::class, parentColumns = ["feedTypeId"], childColumns = ["feedTypeId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["houseId", "strainId", "cDate"]),
        Index(value = ["feedTypeId"])
    ]
)
data class FeedConsumptionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "consumptionId")
    val consumptionId: Long = 0,

    @ColumnInfo(name = "cDate")
    val cDate: LocalDate,

    @ColumnInfo(name = "houseId")
    val houseId: Long,

    @ColumnInfo(name = "strainId")
    val strainId: Long,

    @ColumnInfo(name = "feedTypeId")
    val feedTypeId: Long,

    @ColumnInfo(name = "bagsCount")
    val bagsCount: Double,

    @ColumnInfo(name = "totalWeightKg")
    val totalWeightKg: Double,

    /** تكلفة اللحظة = totalWeightKg × weightedAvgCost وقت التسجيل (Frozen) */
    @ColumnInfo(name = "cost")
    val cost: Double
)

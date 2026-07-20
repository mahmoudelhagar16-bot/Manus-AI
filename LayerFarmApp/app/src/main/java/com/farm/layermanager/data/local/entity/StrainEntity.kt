package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * جدول السلالات (strains)
 * كل سلالة تنتمي لعنبر واحد. لا تُخزَّن أي قيم مشتقة (عمر، بقاء، نافق تراكمي)؛
 * هذه تُحسب دائماً من daily_records عبر Views / UseCases (راجع قسم 12 و14 من وثيقة التحليل).
 */
@Entity(
    tableName = "strains",
    foreignKeys = [
        ForeignKey(
            entity = HouseEntity::class,
            parentColumns = ["houseId"],
            childColumns = ["houseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["houseId"]),
        Index(value = ["status"])
    ]
)
data class StrainEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "strainId")
    val strainId: Long = 0,

    @ColumnInfo(name = "houseId")
    val houseId: Long,

    @ColumnInfo(name = "strainName")
    val strainName: String,

    /** WHITE | RED — يُتحقق منه في طبقة الـ Domain */
    @ColumnInfo(name = "eggColor")
    val eggColor: String,

    @ColumnInfo(name = "arrivalDate")
    val arrivalDate: LocalDate,

    @ColumnInfo(name = "initialChickCount")
    val initialChickCount: Int,

    @ColumnInfo(name = "source")
    val source: String? = null,

    @ColumnInfo(name = "productionStartDate")
    val productionStartDate: LocalDate? = null,

    /** ACTIVE | INACTIVE */
    @ColumnInfo(name = "status")
    val status: String = StrainStatus.ACTIVE.name
)

enum class EggColor { WHITE, RED }
enum class StrainStatus { ACTIVE, INACTIVE }

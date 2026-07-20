package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "vaccinations",
    foreignKeys = [
        ForeignKey(entity = HouseEntity::class, parentColumns = ["houseId"], childColumns = ["houseId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = StrainEntity::class, parentColumns = ["strainId"], childColumns = ["strainId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["strainId", "vDate"]),
        Index(value = ["houseId"])
    ]
)
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "vaccinationId")
    val vaccinationId: Long = 0,

    @ColumnInfo(name = "vDate")
    val vDate: LocalDate,

    @ColumnInfo(name = "houseId")
    val houseId: Long,

    @ColumnInfo(name = "strainId")
    val strainId: Long,

    @ColumnInfo(name = "vaccineName")
    val vaccineName: String,

    @ColumnInfo(name = "company")
    val company: String? = null,

    @ColumnInfo(name = "dose")
    val dose: String? = null,

    @ColumnInfo(name = "method")
    val method: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(entity = HouseEntity::class, parentColumns = ["houseId"], childColumns = ["houseId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = StrainEntity::class, parentColumns = ["strainId"], childColumns = ["strainId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["strainId", "mDate"]),
        Index(value = ["houseId"])
    ]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "medicationId")
    val medicationId: Long = 0,

    @ColumnInfo(name = "mDate")
    val mDate: LocalDate,

    @ColumnInfo(name = "houseId")
    val houseId: Long,

    @ColumnInfo(name = "strainId")
    val strainId: Long,

    @ColumnInfo(name = "medicineName")
    val medicineName: String,

    @ColumnInfo(name = "reason")
    val reason: String? = null,

    @ColumnInfo(name = "dose")
    val dose: String? = null,

    @ColumnInfo(name = "durationDays")
    val durationDays: Int? = null,

    @ColumnInfo(name = "cost")
    val cost: Double = 0.0,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

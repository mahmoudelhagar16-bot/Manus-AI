package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "expense_categories")
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "categoryId")
    val categoryId: Long = 0,

    @ColumnInfo(name = "categoryName")
    val categoryName: String
)

/**
 * houseId اختياري: NULL = مصروف عام على المزرعة (يُوزَّع لاحقاً على العنابر حسب نسبة الطيور).
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = ExpenseCategoryEntity::class, parentColumns = ["categoryId"], childColumns = ["categoryId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = HouseEntity::class, parentColumns = ["houseId"], childColumns = ["houseId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["eDate"]),
        Index(value = ["categoryId"]),
        Index(value = ["houseId"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expenseId")
    val expenseId: Long = 0,

    @ColumnInfo(name = "eDate")
    val eDate: LocalDate,

    @ColumnInfo(name = "categoryId")
    val categoryId: Long,

    @ColumnInfo(name = "houseId")
    val houseId: Long? = null,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "description")
    val description: String? = null
)

@Entity(tableName = "revenue_types")
data class RevenueTypeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "revenueTypeId")
    val revenueTypeId: Long = 0,

    @ColumnInfo(name = "typeName")
    val typeName: String
)

@Entity(
    tableName = "revenues",
    foreignKeys = [
        ForeignKey(entity = RevenueTypeEntity::class, parentColumns = ["revenueTypeId"], childColumns = ["revenueTypeId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["rDate"]),
        Index(value = ["revenueTypeId"])
    ]
)
data class RevenueEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "revenueId")
    val revenueId: Long = 0,

    @ColumnInfo(name = "rDate")
    val rDate: LocalDate,

    @ColumnInfo(name = "revenueTypeId")
    val revenueTypeId: Long,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "description")
    val description: String? = null
)

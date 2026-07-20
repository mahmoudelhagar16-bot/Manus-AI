package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * المخزون العام (أدوية / تحصينات / مطهرات / مستهلكات).
 * منفصل عن مخزون العلف (feed_types) لأنه بطبيعة مختلفة (وحدات متعددة).
 */
@Entity(
    tableName = "inventory_items",
    indices = [Index(value = ["category"])]
)
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "itemId")
    val itemId: Long = 0,

    /** MEDICINE | VACCINE | DISINFECTANT | CONSUMABLE | OTHER */
    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "itemName")
    val itemName: String,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "currentStock")
    val currentStock: Double = 0.0,

    @ColumnInfo(name = "minThreshold")
    val minThreshold: Double = 0.0
)

enum class InventoryCategory { MEDICINE, VACCINE, DISINFECTANT, CONSUMABLE, OTHER }

/**
 * سجل حركات المخزون (Ledger): كل حركة وارد/صادر تُسجَّل هنا،
 * و currentStock في inventory_items يُحدَّث ضمن نفس الـ Transaction.
 * referenceType/referenceId تسمح بربط الحركة بمصدرها (مثال: MEDICATION رقم كذا) دون FK صارم
 * لأن المصدر يمكن أن يكون أي جدول مستقبلي.
 */
@Entity(
    tableName = "inventory_transactions",
    foreignKeys = [
        ForeignKey(entity = InventoryItemEntity::class, parentColumns = ["itemId"], childColumns = ["itemId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["itemId", "tDate"]),
        Index(value = ["referenceType", "referenceId"])
    ]
)
data class InventoryTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transactionId")
    val transactionId: Long = 0,

    @ColumnInfo(name = "itemId")
    val itemId: Long,

    @ColumnInfo(name = "tDate")
    val tDate: LocalDate,

    /** IN | OUT */
    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "referenceType")
    val referenceType: String? = null,

    @ColumnInfo(name = "referenceId")
    val referenceId: Long? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

enum class InventoryTransactionType { IN, OUT }

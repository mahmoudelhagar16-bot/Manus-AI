package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "customers",
    indices = [Index(value = ["name"])]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "customerId")
    val customerId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

/**
 * المبيعات. totalAmount/remainingAmount محسوبة وقت الإدخال في الـ UseCase
 * وتُخزَّن كـ "سعر مجمَّد وقت الصفقة" (Frozen Price) — هذا استثناء مبرَّر محاسبياً
 * (سعر البيع لا يجب أن يتغير بأثر رجعي حتى لو تغيّر سعر الطبق الافتراضي لاحقاً).
 * قيد الكمية المتاحة (لا تتجاوز egg_inventory_balance) يُتحقق منه في RecordSaleUseCase قبل الإدراج.
 */
@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(entity = CustomerEntity::class, parentColumns = ["customerId"], childColumns = ["customerId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [
        Index(value = ["customerId", "sDate"]),
        Index(value = ["sDate"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "saleId")
    val saleId: Long = 0,

    @ColumnInfo(name = "sDate")
    val sDate: LocalDate,

    @ColumnInfo(name = "customerId")
    val customerId: Long,

    @ColumnInfo(name = "whiteTrays")
    val whiteTrays: Double = 0.0,

    @ColumnInfo(name = "redTrays")
    val redTrays: Double = 0.0,

    @ColumnInfo(name = "crackedTrays")
    val crackedTrays: Double = 0.0,

    @ColumnInfo(name = "whitePrice")
    val whitePrice: Double = 0.0,

    @ColumnInfo(name = "redPrice")
    val redPrice: Double = 0.0,

    @ColumnInfo(name = "crackedPrice")
    val crackedPrice: Double = 0.0,

    @ColumnInfo(name = "totalAmount")
    val totalAmount: Double,

    @ColumnInfo(name = "paidAmount")
    val paidAmount: Double = 0.0,

    @ColumnInfo(name = "remainingAmount")
    val remainingAmount: Double,

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

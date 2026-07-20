package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * جدول العنابر (houses)
 * حالياً عنبر واحد فعلياً ضمن استخدام المستخدم، لكن الجدول مصمم لدعم عدة عنابر مستقبلاً.
 */
@Entity(
    tableName = "houses",
    indices = [
        Index(value = ["number"], unique = true),
        Index(value = ["status"])
    ]
)
data class HouseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "houseId")
    val houseId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "number")
    val number: String,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    /** ACTIVE | INACTIVE — يُتحقق منه في طبقة الـ Domain */
    @ColumnInfo(name = "status")
    val status: String = HouseStatus.ACTIVE.name
)

enum class HouseStatus {
    ACTIVE, INACTIVE
}

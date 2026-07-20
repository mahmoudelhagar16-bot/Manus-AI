package com.farm.layermanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * صف واحد ثابت (id = 1) يحمل معلومات الإصدار، يُستخدم عند الاستعادة من نسخة احتياطية
 * لرفض استعادة نسخة أحدث من إصدار التطبيق الحالي.
 */
@Entity(tableName = "schema_metadata")
data class SchemaMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    @ColumnInfo(name = "schemaVersion")
    val schemaVersion: Int,

    @ColumnInfo(name = "lastBackupDate")
    val lastBackupDate: String? = null,

    @ColumnInfo(name = "appVersion")
    val appVersion: String
)

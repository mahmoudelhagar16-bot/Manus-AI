package com.farm.layermanager.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * تخزين التواريخ كـ epochDay (Long) — أخف من String ويدعم الفرز والفلترة بكفاءة عالية
 * ويعمل بشكل ممتاز مع الفهارس (Indexes).
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }
}

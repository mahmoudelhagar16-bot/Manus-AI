package com.farm.layermanager.data.local.database

import androidx.room.migration.Migration

/**
 * كل ترحيل بنيوي مستقبلي (إضافة عمود/جدول/فهرس) يُضاف هنا كـ Migration صريحة
 * (migrate(database: SupportSQLiteDatabase) { database.execSQL(...) })
 * بدلاً من fallbackToDestructiveMigration، حفاظاً على بيانات المستخدم الحقيقية.
 *
 * مثال جاهز للاستخدام عند الحاجة مستقبلاً (معطّل حالياً لأن schemaVersion = 1 فقط):
 *
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         database.execSQL("ALTER TABLE houses ADD COLUMN farmId INTEGER")
 *     }
 * }
 */
object AppDatabaseMigrations {
    val ALL: Array<Migration> = arrayOf(
        // أضف هنا كل Migration جديدة عند رفع version في AppDatabase
    )
}

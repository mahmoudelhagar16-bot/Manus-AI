package com.farm.layermanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.farm.layermanager.data.local.converter.Converters
import com.farm.layermanager.data.local.dao.CustomerDao
import com.farm.layermanager.data.local.dao.DailyRecordDao
import com.farm.layermanager.data.local.dao.ExpenseCategoryDao
import com.farm.layermanager.data.local.dao.ExpenseDao
import com.farm.layermanager.data.local.dao.FeedConsumptionDao
import com.farm.layermanager.data.local.dao.FeedConsumptionTransactionDao
import com.farm.layermanager.data.local.dao.FeedTypeDao
import com.farm.layermanager.data.local.dao.HouseDao
import com.farm.layermanager.data.local.dao.InventoryItemDao
import com.farm.layermanager.data.local.dao.InventoryTransactionCompositeDao
import com.farm.layermanager.data.local.dao.InventoryTransactionDao
import com.farm.layermanager.data.local.dao.MedicationDao
import com.farm.layermanager.data.local.dao.RevenueDao
import com.farm.layermanager.data.local.dao.RevenueTypeDao
import com.farm.layermanager.data.local.dao.SaleDao
import com.farm.layermanager.data.local.dao.SchemaMetadataDao
import com.farm.layermanager.data.local.dao.StrainDao
import com.farm.layermanager.data.local.dao.VaccinationDao
import com.farm.layermanager.data.local.entity.CustomerEntity
import com.farm.layermanager.data.local.entity.DailyRecordEntity
import com.farm.layermanager.data.local.entity.EggInventoryBalanceView
import com.farm.layermanager.data.local.entity.ExpenseCategoryEntity
import com.farm.layermanager.data.local.entity.ExpenseEntity
import com.farm.layermanager.data.local.entity.FeedConsumptionEntity
import com.farm.layermanager.data.local.entity.FeedTypeEntity
import com.farm.layermanager.data.local.entity.HouseEntity
import com.farm.layermanager.data.local.entity.InventoryItemEntity
import com.farm.layermanager.data.local.entity.InventoryTransactionEntity
import com.farm.layermanager.data.local.entity.MedicationEntity
import com.farm.layermanager.data.local.entity.RevenueEntity
import com.farm.layermanager.data.local.entity.RevenueTypeEntity
import com.farm.layermanager.data.local.entity.SaleEntity
import com.farm.layermanager.data.local.entity.SchemaMetadataEntity
import com.farm.layermanager.data.local.entity.StrainCumulativeStatsView
import com.farm.layermanager.data.local.entity.StrainEntity
import com.farm.layermanager.data.local.entity.VaccinationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * قاعدة بيانات Room الرئيسية.
 * schemaVersion = 1 (المرحلة الأولى). أي تعديل بنيوي مستقبلي يجب أن يترافق مع:
 *  1) رفع version هنا.
 *  2) إضافة Migration صريحة في AppDatabaseMigrations (ممنوع fallbackToDestructiveMigration في الإنتاج).
 */
@Database(
    entities = [
        HouseEntity::class,
        StrainEntity::class,
        DailyRecordEntity::class,
        VaccinationEntity::class,
        MedicationEntity::class,
        FeedTypeEntity::class,
        FeedConsumptionEntity::class,
        InventoryItemEntity::class,
        InventoryTransactionEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        ExpenseCategoryEntity::class,
        ExpenseEntity::class,
        RevenueTypeEntity::class,
        RevenueEntity::class,
        SchemaMetadataEntity::class
    ],
    views = [
        EggInventoryBalanceView::class,
        StrainCumulativeStatsView::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun houseDao(): HouseDao
    abstract fun strainDao(): StrainDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun medicationDao(): MedicationDao
    abstract fun feedTypeDao(): FeedTypeDao
    abstract fun feedConsumptionDao(): FeedConsumptionDao
    abstract fun feedConsumptionTransactionDao(): FeedConsumptionTransactionDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun inventoryTransactionDao(): InventoryTransactionDao
    abstract fun inventoryTransactionCompositeDao(): InventoryTransactionCompositeDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun revenueTypeDao(): RevenueTypeDao
    abstract fun revenueDao(): RevenueDao
    abstract fun schemaMetadataDao(): SchemaMetadataDao

    companion object {
        const val DATABASE_NAME = "layer_farm.db"
        private const val CURRENT_SCHEMA_VERSION = 1

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, appVersionName: String): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context, appVersionName).also { INSTANCE = it }
            }
        }

        private fun build(context: Context, appVersionName: String): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(*AppDatabaseMigrations.ALL)
                // ممنوع استخدام fallbackToDestructiveMigration في نسخة الإنتاج: يفقد بيانات المستخدم الحقيقية.
                .addCallback(SeedCallback(context, appVersionName))
                .build()
        }
    }

    /**
     * تُنفَّذ مرة واحدة فقط عند إنشاء قاعدة البيانات لأول مرة (onCreate):
     * تزرع الفئات الافتراضية للمصروفات/الإيرادات (قسم 5.9/5.10) وصف schema_metadata الأولي.
     */
    private class SeedCallback(
        private val context: Context,
        private val appVersionName: String
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val instance = getInstance(context, appVersionName)
                seedDefaultExpenseCategories(instance)
                seedDefaultRevenueTypes(instance)
                instance.schemaMetadataDao().upsert(
                    SchemaMetadataEntity(
                        id = 1,
                        schemaVersion = CURRENT_SCHEMA_VERSION,
                        lastBackupDate = null,
                        appVersion = appVersionName
                    )
                )
            }
        }

        private suspend fun seedDefaultExpenseCategories(db: AppDatabase) {
            val defaults = listOf(
                "مرتبات", "كهرباء", "مياه", "غاز", "نقل",
                "صيانة", "إيجار", "أدوية", "تحصينات", "علف", "أخرى"
            )
            defaults.forEach { name ->
                db.expenseCategoryDao().insert(ExpenseCategoryEntity(categoryName = name))
            }
        }

        private suspend fun seedDefaultRevenueTypes(db: AppDatabase) {
            val defaults = listOf("سبلة", "دجاج مستبعد", "أخرى")
            defaults.forEach { name ->
                db.revenueTypeDao().insert(RevenueTypeEntity(typeName = name))
            }
        }
    }
}

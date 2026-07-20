package com.farm.layermanager.di

import android.content.Context
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
import com.farm.layermanager.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
        return AppDatabase.getInstance(context, versionName)
    }

    @Provides fun provideHouseDao(db: AppDatabase): HouseDao = db.houseDao()
    @Provides fun provideStrainDao(db: AppDatabase): StrainDao = db.strainDao()
    @Provides fun provideDailyRecordDao(db: AppDatabase): DailyRecordDao = db.dailyRecordDao()
    @Provides fun provideVaccinationDao(db: AppDatabase): VaccinationDao = db.vaccinationDao()
    @Provides fun provideMedicationDao(db: AppDatabase): MedicationDao = db.medicationDao()
    @Provides fun provideFeedTypeDao(db: AppDatabase): FeedTypeDao = db.feedTypeDao()
    @Provides fun provideFeedConsumptionDao(db: AppDatabase): FeedConsumptionDao = db.feedConsumptionDao()
    @Provides fun provideFeedConsumptionTransactionDao(db: AppDatabase): FeedConsumptionTransactionDao =
        db.feedConsumptionTransactionDao()
    @Provides fun provideInventoryItemDao(db: AppDatabase): InventoryItemDao = db.inventoryItemDao()
    @Provides fun provideInventoryTransactionDao(db: AppDatabase): InventoryTransactionDao = db.inventoryTransactionDao()
    @Provides fun provideInventoryTransactionCompositeDao(db: AppDatabase): InventoryTransactionCompositeDao =
        db.inventoryTransactionCompositeDao()
    @Provides fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    @Provides fun provideSaleDao(db: AppDatabase): SaleDao = db.saleDao()
    @Provides fun provideExpenseCategoryDao(db: AppDatabase): ExpenseCategoryDao = db.expenseCategoryDao()
    @Provides fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideRevenueTypeDao(db: AppDatabase): RevenueTypeDao = db.revenueTypeDao()
    @Provides fun provideRevenueDao(db: AppDatabase): RevenueDao = db.revenueDao()
    @Provides fun provideSchemaMetadataDao(db: AppDatabase): SchemaMetadataDao = db.schemaMetadataDao()
}

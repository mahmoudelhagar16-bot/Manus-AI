package com.farm.layermanager.di

import com.farm.layermanager.data.repository.CustomerRepositoryImpl
import com.farm.layermanager.data.repository.DailyRecordRepositoryImpl
import com.farm.layermanager.data.repository.ExpenseRepositoryImpl
import com.farm.layermanager.data.repository.FeedRepositoryImpl
import com.farm.layermanager.data.repository.HouseRepositoryImpl
import com.farm.layermanager.data.repository.InventoryRepositoryImpl
import com.farm.layermanager.data.repository.MedicationRepositoryImpl
import com.farm.layermanager.data.repository.RevenueRepositoryImpl
import com.farm.layermanager.data.repository.SaleRepositoryImpl
import com.farm.layermanager.data.repository.StrainRepositoryImpl
import com.farm.layermanager.data.repository.VaccinationRepositoryImpl
import com.farm.layermanager.domain.repository.CustomerRepository
import com.farm.layermanager.domain.repository.DailyRecordRepository
import com.farm.layermanager.domain.repository.ExpenseRepository
import com.farm.layermanager.domain.repository.FeedRepository
import com.farm.layermanager.domain.repository.HouseRepository
import com.farm.layermanager.domain.repository.InventoryRepository
import com.farm.layermanager.domain.repository.MedicationRepository
import com.farm.layermanager.domain.repository.RevenueRepository
import com.farm.layermanager.domain.repository.SaleRepository
import com.farm.layermanager.domain.repository.StrainRepository
import com.farm.layermanager.domain.repository.VaccinationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindHouseRepository(impl: HouseRepositoryImpl): HouseRepository

    @Binds @Singleton
    abstract fun bindStrainRepository(impl: StrainRepositoryImpl): StrainRepository

    @Binds @Singleton
    abstract fun bindDailyRecordRepository(impl: DailyRecordRepositoryImpl): DailyRecordRepository

    @Binds @Singleton
    abstract fun bindVaccinationRepository(impl: VaccinationRepositoryImpl): VaccinationRepository

    @Binds @Singleton
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds @Singleton
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository

    @Binds @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds @Singleton
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository

    @Binds @Singleton
    abstract fun bindSaleRepository(impl: SaleRepositoryImpl): SaleRepository

    @Binds @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds @Singleton
    abstract fun bindRevenueRepository(impl: RevenueRepositoryImpl): RevenueRepository
}

package com.farm.layermanager.di

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
import com.farm.layermanager.domain.usecase.dailyrecord.AddDailyRecordUseCase
import com.farm.layermanager.domain.usecase.dailyrecord.DeleteDailyRecordUseCase
import com.farm.layermanager.domain.usecase.dailyrecord.GetDailyRecordsUseCase
import com.farm.layermanager.domain.usecase.dailyrecord.UpdateDailyRecordUseCase
import com.farm.layermanager.domain.usecase.feed.AddFeedPurchaseUseCase
import com.farm.layermanager.domain.usecase.feed.AddFeedTypeUseCase
import com.farm.layermanager.domain.usecase.feed.GetFeedUseCase
import com.farm.layermanager.domain.usecase.feed.RecordFeedConsumptionUseCase
import com.farm.layermanager.domain.usecase.finance.AddExpenseUseCase
import com.farm.layermanager.domain.usecase.finance.AddRevenueUseCase
import com.farm.layermanager.domain.usecase.finance.AllocateGeneralExpenseUseCase
import com.farm.layermanager.domain.usecase.finance.GetExpenseCategoriesUseCase
import com.farm.layermanager.domain.usecase.finance.GetFinanceUseCase
import com.farm.layermanager.domain.usecase.finance.GetNetProfitUseCase
import com.farm.layermanager.domain.usecase.health.AddMedicationUseCase
import com.farm.layermanager.domain.usecase.health.AddVaccinationUseCase
import com.farm.layermanager.domain.usecase.health.GetMedicationsUseCase
import com.farm.layermanager.domain.usecase.health.GetVaccinationsUseCase
import com.farm.layermanager.domain.usecase.house.AddHouseUseCase
import com.farm.layermanager.domain.usecase.house.DeactivateHouseUseCase
import com.farm.layermanager.domain.usecase.house.GetHousesUseCase
import com.farm.layermanager.domain.usecase.house.ReactivateHouseUseCase
import com.farm.layermanager.domain.usecase.house.UpdateHouseUseCase
import com.farm.layermanager.domain.usecase.inventory.AddInventoryItemUseCase
import com.farm.layermanager.domain.usecase.inventory.GetInventoryUseCase
import com.farm.layermanager.domain.usecase.inventory.RecordInventoryTransactionUseCase
import com.farm.layermanager.domain.usecase.reports.GenerateReportUseCase
import com.farm.layermanager.domain.usecase.reports.GetDashboardUseCase
import com.farm.layermanager.domain.usecase.reports.SearchAllUseCase
import com.farm.layermanager.domain.usecase.sales.AddCustomerUseCase
import com.farm.layermanager.domain.usecase.sales.GetCustomersUseCase
import com.farm.layermanager.domain.usecase.sales.GetSalesUseCase
import com.farm.layermanager.domain.usecase.sales.RecordSaleUseCase
import com.farm.layermanager.domain.usecase.strain.AddStrainUseCase
import com.farm.layermanager.domain.usecase.strain.DeactivateStrainUseCase
import com.farm.layermanager.domain.usecase.strain.GetStrainDetailsUseCase
import com.farm.layermanager.domain.usecase.strain.UpdateStrainUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * كل الـ UseCases هي classes عادية (لا interfaces)، لذا تُوفَّر عبر @Provides وليس @Binds.
 * @ViewModelScoped ليس ضرورياً هنا لأنها Stateless بالكامل — @Singleton كافٍ ويوفّر Instance واحد فقط.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // ---------- House ----------
    @Provides @Singleton
    fun provideAddHouseUseCase(repo: HouseRepository) = AddHouseUseCase(repo)

    @Provides @Singleton
    fun provideUpdateHouseUseCase(repo: HouseRepository) = UpdateHouseUseCase(repo)

    @Provides @Singleton
    fun provideDeactivateHouseUseCase(repo: HouseRepository) = DeactivateHouseUseCase(repo)

    @Provides @Singleton
    fun provideReactivateHouseUseCase(repo: HouseRepository) = ReactivateHouseUseCase(repo)

    @Provides @Singleton
    fun provideGetHousesUseCase(repo: HouseRepository) = GetHousesUseCase(repo)

    // ---------- Strain ----------
    @Provides @Singleton
    fun provideAddStrainUseCase(repo: StrainRepository) = AddStrainUseCase(repo)

    @Provides @Singleton
    fun provideUpdateStrainUseCase(repo: StrainRepository) = UpdateStrainUseCase(repo)

    @Provides @Singleton
    fun provideDeactivateStrainUseCase(repo: StrainRepository) = DeactivateStrainUseCase(repo)

    @Provides @Singleton
    fun provideGetStrainDetailsUseCase(repo: StrainRepository) = GetStrainDetailsUseCase(repo)

    // ---------- Daily Record ----------
    @Provides @Singleton
    fun provideAddDailyRecordUseCase(dailyRepo: DailyRecordRepository, strainRepo: StrainRepository) =
        AddDailyRecordUseCase(dailyRepo, strainRepo)

    @Provides @Singleton
    fun provideUpdateDailyRecordUseCase(dailyRepo: DailyRecordRepository, strainRepo: StrainRepository) =
        UpdateDailyRecordUseCase(dailyRepo, strainRepo)

    @Provides @Singleton
    fun provideDeleteDailyRecordUseCase(repo: DailyRecordRepository) = DeleteDailyRecordUseCase(repo)

    @Provides @Singleton
    fun provideGetDailyRecordsUseCase(repo: DailyRecordRepository) = GetDailyRecordsUseCase(repo)

    // ---------- Health ----------
    @Provides @Singleton
    fun provideAddVaccinationUseCase(repo: VaccinationRepository) = AddVaccinationUseCase(repo)

    @Provides @Singleton
    fun provideGetVaccinationsUseCase(repo: VaccinationRepository) = GetVaccinationsUseCase(repo)

    @Provides @Singleton
    fun provideAddMedicationUseCase(repo: MedicationRepository) = AddMedicationUseCase(repo)

    @Provides @Singleton
    fun provideGetMedicationsUseCase(repo: MedicationRepository) = GetMedicationsUseCase(repo)

    // ---------- Feed ----------
    @Provides @Singleton
    fun provideAddFeedTypeUseCase(repo: FeedRepository) = AddFeedTypeUseCase(repo)

    @Provides @Singleton
    fun provideAddFeedPurchaseUseCase(repo: FeedRepository) = AddFeedPurchaseUseCase(repo)

    @Provides @Singleton
    fun provideRecordFeedConsumptionUseCase(repo: FeedRepository) = RecordFeedConsumptionUseCase(repo)

    @Provides @Singleton
    fun provideGetFeedUseCase(repo: FeedRepository) = GetFeedUseCase(repo)

    // ---------- Inventory ----------
    @Provides @Singleton
    fun provideAddInventoryItemUseCase(repo: InventoryRepository) = AddInventoryItemUseCase(repo)

    @Provides @Singleton
    fun provideRecordInventoryTransactionUseCase(repo: InventoryRepository) = RecordInventoryTransactionUseCase(repo)

    @Provides @Singleton
    fun provideGetInventoryUseCase(repo: InventoryRepository) = GetInventoryUseCase(repo)

    // ---------- Sales ----------
    @Provides @Singleton
    fun provideAddCustomerUseCase(repo: CustomerRepository) = AddCustomerUseCase(repo)

    @Provides @Singleton
    fun provideGetCustomersUseCase(repo: CustomerRepository) = GetCustomersUseCase(repo)

    @Provides @Singleton
    fun provideRecordSaleUseCase(repo: SaleRepository) = RecordSaleUseCase(repo)

    @Provides @Singleton
    fun provideGetSalesUseCase(repo: SaleRepository) = GetSalesUseCase(repo)

    // ---------- Finance ----------
    @Provides @Singleton
    fun provideAddExpenseUseCase(repo: ExpenseRepository) = AddExpenseUseCase(repo)

    @Provides @Singleton
    fun provideAddRevenueUseCase(repo: RevenueRepository) = AddRevenueUseCase(repo)

    @Provides @Singleton
    fun provideGetExpenseCategoriesUseCase(repo: ExpenseRepository) = GetExpenseCategoriesUseCase(repo)

    @Provides @Singleton
    fun provideAllocateGeneralExpenseUseCase(strainRepo: StrainRepository) = AllocateGeneralExpenseUseCase(strainRepo)

    @Provides @Singleton
    fun provideGetFinanceUseCase(expenseRepo: ExpenseRepository, revenueRepo: RevenueRepository) =
        GetFinanceUseCase(expenseRepo, revenueRepo)

    @Provides @Singleton
    fun provideGetNetProfitUseCase(
        saleRepo: SaleRepository,
        expenseRepo: ExpenseRepository,
        revenueRepo: RevenueRepository
    ) = GetNetProfitUseCase(saleRepo, expenseRepo, revenueRepo)

    // ---------- Reports ----------
    @Provides @Singleton
    fun provideGetDashboardUseCase(
        houseRepo: HouseRepository,
        strainRepo: StrainRepository,
        dailyRepo: DailyRecordRepository,
        saleRepo: SaleRepository,
        expenseRepo: ExpenseRepository,
        revenueRepo: RevenueRepository
    ) = GetDashboardUseCase(houseRepo, strainRepo, dailyRepo, saleRepo, expenseRepo, revenueRepo)

    @Provides @Singleton
    fun provideGenerateReportUseCase(
        dailyRepo: DailyRecordRepository,
        feedRepo: FeedRepository,
        medicationRepo: MedicationRepository,
        saleRepo: SaleRepository,
        expenseRepo: ExpenseRepository,
        revenueRepo: RevenueRepository
    ) = GenerateReportUseCase(dailyRepo, feedRepo, medicationRepo, saleRepo, expenseRepo, revenueRepo)

    @Provides @Singleton
    fun provideSearchAllUseCase(
        strainRepo: StrainRepository,
        customerRepo: CustomerRepository,
        saleRepo: SaleRepository
    ) = SearchAllUseCase(strainRepo, customerRepo, saleRepo)
}

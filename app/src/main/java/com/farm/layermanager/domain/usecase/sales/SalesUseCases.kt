package com.farm.layermanager.domain.usecase.sales

import com.farm.layermanager.domain.common.DomainResult
import com.farm.layermanager.domain.common.Validator
import com.farm.layermanager.domain.model.Customer
import com.farm.layermanager.domain.model.Sale
import com.farm.layermanager.domain.repository.CustomerRepository
import com.farm.layermanager.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow

class AddCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customer: Customer): DomainResult<Long> {
        Validator.requireNotBlank(customer.name, "اسم العميل")?.let { return DomainResult.Error(it) }

        val id = repository.insert(customer)
        return DomainResult.Success(id)
    }
}

class GetCustomersUseCase(private val repository: CustomerRepository) {
    fun getAll(): Flow<List<Customer>> = repository.getAll()
    fun getById(customerId: Long): Flow<Customer?> = repository.getById(customerId)
    fun getTotalDebt(customerId: Long): Flow<Double> = repository.getTotalDebtForCustomer(customerId)
}

/**
 * UC-13 + UC-14: تسجيل بيع مع دفعات جزئية، ومنع بيع كمية أكبر من الرصيد الفعلي المتاح
 * (View: egg_inventory_balance، قسم 12 و 5.8).
 * totalAmount / remainingAmount تُحسب هنا وتُخزَّن كسعر "مجمَّد وقت الصفقة" (Frozen)، وليس مشتقاً لاحقاً.
 */
class RecordSaleUseCase(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(
        customerId: Long,
        sDate: java.time.LocalDate,
        whiteTrays: Double,
        redTrays: Double,
        crackedTrays: Double,
        whitePrice: Double,
        redPrice: Double,
        crackedPrice: Double,
        paidAmount: Double,
        paymentMethod: String?,
        notes: String?
    ): DomainResult<Long> {
        val totalTraysRequested = whiteTrays + redTrays + crackedTrays
        Validator.requirePositive(totalTraysRequested, "إجمالي الأطباق")?.let { return DomainResult.Error(it) }
        Validator.requireNonNegative(paidAmount, "المبلغ المدفوع")?.let { return DomainResult.Error(it) }

        val balance = saleRepository.getEggInventoryBalanceOnce()
        val available = balance?.availableTrays ?: 0.0
        if (totalTraysRequested > available) {
            return DomainResult.Error(
                "الكمية المطلوب بيعها ($totalTraysRequested طبق) أكبر من الرصيد المتاح فعلياً ($available طبق)"
            )
        }

        val totalAmount = (whiteTrays * whitePrice) + (redTrays * redPrice) + (crackedTrays * crackedPrice)
        if (paidAmount > totalAmount) {
            return DomainResult.Error("المبلغ المدفوع ($paidAmount) أكبر من إجمالي قيمة الفاتورة ($totalAmount)")
        }
        val remainingAmount = totalAmount - paidAmount

        val sale = Sale(
            sDate = sDate,
            customerId = customerId,
            whiteTrays = whiteTrays,
            redTrays = redTrays,
            crackedTrays = crackedTrays,
            whitePrice = whitePrice,
            redPrice = redPrice,
            crackedPrice = crackedPrice,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            paymentMethod = paymentMethod,
            notes = notes
        )

        val id = saleRepository.insert(sale)
        return DomainResult.Success(id)
    }
}

class GetSalesUseCase(private val repository: SaleRepository) {
    fun getAll(): Flow<List<Sale>> = repository.getAll()
    fun getByCustomer(customerId: Long): Flow<List<Sale>> = repository.getByCustomer(customerId)
    fun getEggInventoryBalance() = repository.getEggInventoryBalance()
    fun getTotalOutstandingDebt(): Flow<Double> = repository.getTotalOutstandingDebt()
}

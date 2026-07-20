package com.farm.layermanager.domain.usecase.reports

import com.farm.layermanager.domain.model.Customer
import com.farm.layermanager.domain.model.Sale
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.repository.CustomerRepository
import com.farm.layermanager.domain.repository.SaleRepository
import com.farm.layermanager.domain.repository.StrainRepository
import kotlinx.coroutines.flow.combine

data class SearchResults(
    val strains: List<Strain>,
    val customers: List<Customer>,
    val sales: List<Sale>
)

/**
 * UC-20: بحث موحّد أولي عبر السلالات/العملاء/المبيعات (أكثر الجداول استخداماً للبحث اليدوي).
 * ملاحظة أداء: هذا التنفيذ الأولي يفلتر داخل الذاكرة عبر Flow.map؛ عندما تتجاوز السجلات آلافاً
 * (كما يتطلب قسم 1.2 "الأداء")، يُستحسن استبداله باستعلامات Room بصيغة `LIKE '%query%'` مفهرسة
 * مباشرة في كل DAO (strainName, customer.name, notes...) بدل الفلترة في Kotlin.
 */
class SearchAllUseCase(
    private val strainRepository: StrainRepository,
    private val customerRepository: CustomerRepository,
    private val saleRepository: SaleRepository
) {
    operator fun invoke(query: String) = combine(
        strainRepository.getAll(),
        customerRepository.getAll(),
        saleRepository.getAll()
    ) { strains, customers, sales ->
        val q = query.trim()
        if (q.isEmpty()) {
            SearchResults(emptyList(), emptyList(), emptyList())
        } else {
            SearchResults(
                strains = strains.filter { it.strainName.contains(q, ignoreCase = true) },
                customers = customers.filter {
                    it.name.contains(q, ignoreCase = true) || (it.phone?.contains(q) == true)
                },
                sales = sales.filter { it.notes?.contains(q, ignoreCase = true) == true }
            )
        }
    }
}

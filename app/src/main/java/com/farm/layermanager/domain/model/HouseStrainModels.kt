package com.farm.layermanager.domain.model

import java.time.LocalDate

data class House(
    val houseId: Long = 0,
    val name: String,
    val number: String,
    val notes: String? = null,
    val status: HouseStatus = HouseStatus.ACTIVE
)

enum class HouseStatus { ACTIVE, INACTIVE }

data class Strain(
    val strainId: Long = 0,
    val houseId: Long,
    val strainName: String,
    val eggColor: EggColor,
    val arrivalDate: LocalDate,
    val initialChickCount: Int,
    val source: String? = null,
    val productionStartDate: LocalDate? = null,
    val status: StrainStatus = StrainStatus.ACTIVE
)

enum class EggColor { WHITE, RED }
enum class StrainStatus { ACTIVE, INACTIVE }

/**
 * لقطة مؤشرات مشتقة لسلالة معيّنة، تُبنى دائماً وقت الطلب (لا تُخزَّن) — راجع قسم 12 و14.
 */
data class StrainStats(
    val strain: Strain,
    val ageInWeeks: Long,
    val cumulativeMortality: Int,
    val cumulativeCulled: Int,
    val currentBirdCount: Int,
    val livabilityPercent: Double,
    val mortalityPercent: Double
)

package com.farm.layermanager.domain.model

import java.time.LocalDate

data class DailyRecord(
    val recordId: Long = 0,
    val recordDate: LocalDate,
    val houseId: Long,
    val strainId: Long,
    val liveBirds: Int,
    val mortality: Int,
    val culled: Int,
    val feedQtyKg: Double,
    val waterLiters: Double,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val lightHours: Double? = null,
    val productionTrays: Double,
    val crackedEggs: Int = 0,
    val deformedEggs: Int = 0,
    val floorEggs: Int = 0,
    val notes: String? = null
) {
    /** عدد البيض المنتج = productionTrays × 30 (قسم 12) */
    fun producedEggsCount(eggsPerTray: Int = 30): Int = (productionTrays * eggsPerTray).toInt()
}

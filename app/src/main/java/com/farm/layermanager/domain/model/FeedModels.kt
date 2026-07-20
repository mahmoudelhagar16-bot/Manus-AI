package com.farm.layermanager.domain.model

import java.time.LocalDate

data class FeedType(
    val feedTypeId: Long = 0,
    val feedName: String,
    val company: String? = null,
    val price: Double,
    val bagWeightKg: Double,
    val purchaseDate: LocalDate,
    val quantityKg: Double,
    val currentStockKg: Double,
    val weightedAvgCost: Double
)

data class FeedConsumption(
    val consumptionId: Long = 0,
    val cDate: LocalDate,
    val houseId: Long,
    val strainId: Long,
    val feedTypeId: Long,
    val bagsCount: Double,
    val totalWeightKg: Double,
    val cost: Double
)

package com.farm.layermanager.data.mapper

import com.farm.layermanager.data.local.entity.FeedConsumptionEntity
import com.farm.layermanager.data.local.entity.FeedTypeEntity
import com.farm.layermanager.domain.model.FeedConsumption
import com.farm.layermanager.domain.model.FeedType

fun FeedTypeEntity.toDomain(): FeedType = FeedType(
    feedTypeId = feedTypeId,
    feedName = feedName,
    company = company,
    price = price,
    bagWeightKg = bagWeightKg,
    purchaseDate = purchaseDate,
    quantityKg = quantityKg,
    currentStockKg = currentStockKg,
    weightedAvgCost = weightedAvgCost
)

fun FeedType.toEntity(): FeedTypeEntity = FeedTypeEntity(
    feedTypeId = feedTypeId,
    feedName = feedName,
    company = company,
    price = price,
    bagWeightKg = bagWeightKg,
    purchaseDate = purchaseDate,
    quantityKg = quantityKg,
    currentStockKg = currentStockKg,
    weightedAvgCost = weightedAvgCost
)

fun FeedConsumptionEntity.toDomain(): FeedConsumption = FeedConsumption(
    consumptionId = consumptionId,
    cDate = cDate,
    houseId = houseId,
    strainId = strainId,
    feedTypeId = feedTypeId,
    bagsCount = bagsCount,
    totalWeightKg = totalWeightKg,
    cost = cost
)

fun FeedConsumption.toEntity(): FeedConsumptionEntity = FeedConsumptionEntity(
    consumptionId = consumptionId,
    cDate = cDate,
    houseId = houseId,
    strainId = strainId,
    feedTypeId = feedTypeId,
    bagsCount = bagsCount,
    totalWeightKg = totalWeightKg,
    cost = cost
)

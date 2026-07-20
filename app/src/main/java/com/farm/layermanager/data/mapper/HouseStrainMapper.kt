package com.farm.layermanager.data.mapper

import com.farm.layermanager.data.local.entity.HouseEntity
import com.farm.layermanager.data.local.entity.StrainCumulativeStatsView
import com.farm.layermanager.data.local.entity.StrainEntity
import com.farm.layermanager.domain.model.EggColor
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.HouseStatus
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.model.StrainStatus
import com.farm.layermanager.domain.repository.StrainCumulativeStats

fun HouseEntity.toDomain(): House = House(
    houseId = houseId,
    name = name,
    number = number,
    notes = notes,
    status = runCatching { HouseStatus.valueOf(status) }.getOrDefault(HouseStatus.ACTIVE)
)

fun House.toEntity(): HouseEntity = HouseEntity(
    houseId = houseId,
    name = name,
    number = number,
    notes = notes,
    status = status.name
)

fun StrainEntity.toDomain(): Strain = Strain(
    strainId = strainId,
    houseId = houseId,
    strainName = strainName,
    eggColor = runCatching { EggColor.valueOf(eggColor) }.getOrDefault(EggColor.WHITE),
    arrivalDate = arrivalDate,
    initialChickCount = initialChickCount,
    source = source,
    productionStartDate = productionStartDate,
    status = runCatching { StrainStatus.valueOf(status) }.getOrDefault(StrainStatus.ACTIVE)
)

fun Strain.toEntity(): StrainEntity = StrainEntity(
    strainId = strainId,
    houseId = houseId,
    strainName = strainName,
    eggColor = eggColor.name,
    arrivalDate = arrivalDate,
    initialChickCount = initialChickCount,
    source = source,
    productionStartDate = productionStartDate,
    status = status.name
)

fun StrainCumulativeStatsView.toDomain(): StrainCumulativeStats = StrainCumulativeStats(
    strainId = strainId,
    initialChickCount = initialChickCount,
    totalMortality = totalMortality,
    totalCulled = totalCulled,
    currentBirds = currentBirds
)

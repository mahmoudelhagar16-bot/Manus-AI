package com.farm.layermanager.data.mapper

import com.farm.layermanager.data.local.entity.DailyRecordEntity
import com.farm.layermanager.domain.model.DailyRecord

fun DailyRecordEntity.toDomain(): DailyRecord = DailyRecord(
    recordId = recordId,
    recordDate = recordDate,
    houseId = houseId,
    strainId = strainId,
    liveBirds = liveBirds,
    mortality = mortality,
    culled = culled,
    feedQtyKg = feedQtyKg,
    waterLiters = waterLiters,
    temperature = temperature,
    humidity = humidity,
    lightHours = lightHours,
    productionTrays = productionTrays,
    crackedEggs = crackedEggs,
    deformedEggs = deformedEggs,
    floorEggs = floorEggs,
    notes = notes
)

fun DailyRecord.toEntity(): DailyRecordEntity = DailyRecordEntity(
    recordId = recordId,
    recordDate = recordDate,
    houseId = houseId,
    strainId = strainId,
    liveBirds = liveBirds,
    mortality = mortality,
    culled = culled,
    feedQtyKg = feedQtyKg,
    waterLiters = waterLiters,
    temperature = temperature,
    humidity = humidity,
    lightHours = lightHours,
    productionTrays = productionTrays,
    crackedEggs = crackedEggs,
    deformedEggs = deformedEggs,
    floorEggs = floorEggs,
    notes = notes
)

package com.farm.layermanager.data.mapper

import com.farm.layermanager.data.local.entity.MedicationEntity
import com.farm.layermanager.data.local.entity.VaccinationEntity
import com.farm.layermanager.domain.model.Medication
import com.farm.layermanager.domain.model.Vaccination

fun VaccinationEntity.toDomain(): Vaccination = Vaccination(
    vaccinationId = vaccinationId,
    vDate = vDate,
    houseId = houseId,
    strainId = strainId,
    vaccineName = vaccineName,
    company = company,
    dose = dose,
    method = method,
    notes = notes
)

fun Vaccination.toEntity(): VaccinationEntity = VaccinationEntity(
    vaccinationId = vaccinationId,
    vDate = vDate,
    houseId = houseId,
    strainId = strainId,
    vaccineName = vaccineName,
    company = company,
    dose = dose,
    method = method,
    notes = notes
)

fun MedicationEntity.toDomain(): Medication = Medication(
    medicationId = medicationId,
    mDate = mDate,
    houseId = houseId,
    strainId = strainId,
    medicineName = medicineName,
    reason = reason,
    dose = dose,
    durationDays = durationDays,
    cost = cost,
    notes = notes
)

fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    medicationId = medicationId,
    mDate = mDate,
    houseId = houseId,
    strainId = strainId,
    medicineName = medicineName,
    reason = reason,
    dose = dose,
    durationDays = durationDays,
    cost = cost,
    notes = notes
)

package com.farm.layermanager.domain.model

import java.time.LocalDate

data class Vaccination(
    val vaccinationId: Long = 0,
    val vDate: LocalDate,
    val houseId: Long,
    val strainId: Long,
    val vaccineName: String,
    val company: String? = null,
    val dose: String? = null,
    val method: String? = null,
    val notes: String? = null
)

data class Medication(
    val medicationId: Long = 0,
    val mDate: LocalDate,
    val houseId: Long,
    val strainId: Long,
    val medicineName: String,
    val reason: String? = null,
    val dose: String? = null,
    val durationDays: Int? = null,
    val cost: Double = 0.0,
    val notes: String? = null
)

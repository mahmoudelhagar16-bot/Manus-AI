package com.farm.layermanager.data.mapper

import com.farm.layermanager.data.local.entity.CustomerEntity
import com.farm.layermanager.data.local.entity.EggInventoryBalanceView
import com.farm.layermanager.data.local.entity.SaleEntity
import com.farm.layermanager.domain.model.Customer
import com.farm.layermanager.domain.model.Sale
import com.farm.layermanager.domain.repository.EggInventoryBalance

fun CustomerEntity.toDomain(): Customer = Customer(
    customerId = customerId,
    name = name,
    phone = phone,
    address = address,
    notes = notes
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    customerId = customerId,
    name = name,
    phone = phone,
    address = address,
    notes = notes
)

fun SaleEntity.toDomain(): Sale = Sale(
    saleId = saleId,
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

fun Sale.toEntity(): SaleEntity = SaleEntity(
    saleId = saleId,
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

fun EggInventoryBalanceView.toDomain(): EggInventoryBalance = EggInventoryBalance(
    totalProducedTrays = totalProducedTrays,
    totalSoldTrays = totalSoldTrays,
    availableTrays = availableTrays
)

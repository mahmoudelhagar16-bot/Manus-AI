package com.farm.layermanager.data.mapper

import com.farm.layermanager.data.local.dao.CategoryTotal as CategoryTotalEntity
import com.farm.layermanager.data.local.entity.ExpenseCategoryEntity
import com.farm.layermanager.data.local.entity.ExpenseEntity
import com.farm.layermanager.data.local.entity.RevenueEntity
import com.farm.layermanager.data.local.entity.RevenueTypeEntity
import com.farm.layermanager.domain.model.Expense
import com.farm.layermanager.domain.model.ExpenseCategory
import com.farm.layermanager.domain.model.Revenue
import com.farm.layermanager.domain.model.RevenueType
import com.farm.layermanager.domain.repository.CategoryTotal

fun ExpenseCategoryEntity.toDomain(): ExpenseCategory = ExpenseCategory(
    categoryId = categoryId,
    categoryName = categoryName
)

fun ExpenseCategory.toEntity(): ExpenseCategoryEntity = ExpenseCategoryEntity(
    categoryId = categoryId,
    categoryName = categoryName
)

fun ExpenseEntity.toDomain(): Expense = Expense(
    expenseId = expenseId,
    eDate = eDate,
    categoryId = categoryId,
    houseId = houseId,
    amount = amount,
    description = description
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    expenseId = expenseId,
    eDate = eDate,
    categoryId = categoryId,
    houseId = houseId,
    amount = amount,
    description = description
)

fun RevenueTypeEntity.toDomain(): RevenueType = RevenueType(
    revenueTypeId = revenueTypeId,
    typeName = typeName
)

fun RevenueType.toEntity(): RevenueTypeEntity = RevenueTypeEntity(
    revenueTypeId = revenueTypeId,
    typeName = typeName
)

fun RevenueEntity.toDomain(): Revenue = Revenue(
    revenueId = revenueId,
    rDate = rDate,
    revenueTypeId = revenueTypeId,
    amount = amount,
    description = description
)

fun Revenue.toEntity(): RevenueEntity = RevenueEntity(
    revenueId = revenueId,
    rDate = rDate,
    revenueTypeId = revenueTypeId,
    amount = amount,
    description = description
)

fun CategoryTotalEntity.toDomain(): CategoryTotal = CategoryTotal(
    categoryName = categoryName,
    total = total
)

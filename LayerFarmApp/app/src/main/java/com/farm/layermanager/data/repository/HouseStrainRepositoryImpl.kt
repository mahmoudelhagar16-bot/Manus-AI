package com.farm.layermanager.data.repository

import com.farm.layermanager.data.local.dao.HouseDao
import com.farm.layermanager.data.local.dao.StrainDao
import com.farm.layermanager.data.mapper.toDomain
import com.farm.layermanager.data.mapper.toEntity
import com.farm.layermanager.domain.model.House
import com.farm.layermanager.domain.model.Strain
import com.farm.layermanager.domain.repository.HouseRepository
import com.farm.layermanager.domain.repository.StrainCumulativeStats
import com.farm.layermanager.domain.repository.StrainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HouseRepositoryImpl @Inject constructor(
    private val dao: HouseDao
) : HouseRepository {

    override suspend fun insert(house: House): Long = dao.insert(house.toEntity())

    override suspend fun update(house: House) = dao.update(house.toEntity())

    override suspend fun setStatus(houseId: Long, status: String) = dao.updateStatus(houseId, status)

    override fun getAll(): Flow<List<House>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getActive(): Flow<List<House>> = dao.getActive().map { list -> list.map { it.toDomain() } }

    override fun getById(houseId: Long): Flow<House?> = dao.getById(houseId).map { it?.toDomain() }

    override suspend fun hasRelatedDailyRecords(houseId: Long): Boolean =
        dao.countRelatedDailyRecords(houseId) > 0
}

class StrainRepositoryImpl @Inject constructor(
    private val dao: StrainDao
) : StrainRepository {

    override suspend fun insert(strain: Strain): Long = dao.insert(strain.toEntity())

    override suspend fun update(strain: Strain) = dao.update(strain.toEntity())

    override suspend fun setStatus(strainId: Long, status: String) = dao.updateStatus(strainId, status)

    override fun getByHouse(houseId: Long): Flow<List<Strain>> =
        dao.getByHouse(houseId).map { list -> list.map { it.toDomain() } }

    override fun getActive(): Flow<List<Strain>> = dao.getActive().map { list -> list.map { it.toDomain() } }

    override fun getAll(): Flow<List<Strain>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(strainId: Long): Flow<Strain?> = dao.getById(strainId).map { it?.toDomain() }

    override fun getCumulativeStats(strainId: Long): Flow<StrainCumulativeStats?> =
        dao.getCumulativeStats(strainId).map { it?.toDomain() }

    override fun getAllCumulativeStats(): Flow<List<StrainCumulativeStats>> =
        dao.getAllCumulativeStats().map { list -> list.map { it.toDomain() } }
}

package com.alfath.qrscreenscanner.data.repository

import com.alfath.qrscreenscanner.data.local.ScanHistoryDao
import com.alfath.qrscreenscanner.data.local.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val dao: ScanHistoryDao) {

    fun getAllScans(): Flow<List<ScanHistoryEntity>> = dao.getAllScans()

    fun searchScans(query: String): Flow<List<ScanHistoryEntity>> = dao.searchScans(query)

    suspend fun insertScan(scan: ScanHistoryEntity): Long = dao.insertScan(scan)

    suspend fun deleteScan(scan: ScanHistoryEntity) = dao.deleteScan(scan)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAllHistory()
}

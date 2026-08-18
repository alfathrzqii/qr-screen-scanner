package dev.alfathrzqii.qrscreenscanner.data.repository

import dev.alfathrzqii.qrscreenscanner.data.local.QrContentType
import dev.alfathrzqii.qrscreenscanner.data.local.ScanHistoryDao
import dev.alfathrzqii.qrscreenscanner.data.local.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val dao: ScanHistoryDao) {

    fun getAllScans(): Flow<List<ScanHistoryEntity>> = dao.getAllScans()

    fun searchScans(query: String): Flow<List<ScanHistoryEntity>> = dao.searchScans(query)

    fun getScansByType(type: QrContentType): Flow<List<ScanHistoryEntity>> = dao.getScansByType(type)

    suspend fun insertScan(scan: ScanHistoryEntity): Long = dao.insertScan(scan)

    suspend fun deleteScan(scan: ScanHistoryEntity) = dao.deleteScan(scan)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAllHistory()
}

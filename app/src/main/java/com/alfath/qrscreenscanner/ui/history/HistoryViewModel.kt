package com.alfath.qrscreenscanner.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alfath.qrscreenscanner.data.local.AppDatabase
import com.alfath.qrscreenscanner.data.local.ScanHistoryEntity
import com.alfath.qrscreenscanner.data.repository.ScanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ScanRepository(db.scanHistoryDao())
    }

    val historyList: StateFlow<List<ScanHistoryEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllScans()
            } else {
                repository.searchScans(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteScan(scan: ScanHistoryEntity) {
        viewModelScope.launch {
            repository.deleteScan(scan)
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun saveScan(scan: ScanHistoryEntity) {
        viewModelScope.launch {
            repository.insertScan(scan)
        }
    }
}

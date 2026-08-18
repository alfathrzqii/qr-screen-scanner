package dev.alfathrzqii.qrscreenscanner.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.alfathrzqii.qrscreenscanner.data.local.AppDatabase
import dev.alfathrzqii.qrscreenscanner.data.local.QrContentType
import dev.alfathrzqii.qrscreenscanner.data.local.ScanHistoryEntity
import dev.alfathrzqii.qrscreenscanner.data.repository.ScanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScanRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<QrContentType?>(null)
    val selectedFilter: StateFlow<QrContentType?> = _selectedFilter.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ScanRepository(db.scanHistoryDao())
    }

    val historyList: StateFlow<List<ScanHistoryEntity>> = combine(
        _searchQuery,
        _selectedFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isNotBlank()) {
            repository.searchScans(query)
        } else if (filter != null) {
            repository.getScansByType(filter)
        } else {
            repository.getAllScans()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFilter(filter: QrContentType?) {
        _selectedFilter.value = filter
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

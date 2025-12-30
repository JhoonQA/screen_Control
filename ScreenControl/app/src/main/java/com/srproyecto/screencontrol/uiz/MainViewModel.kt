package com.srproyecto.screencontrol.uiz

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srproyecto.screencontrol.data.AppDatabase
import com.srproyecto.screencontrol.data.AppLimit
import com.srproyecto.screencontrol.data.AppUsageInfo
import com.srproyecto.screencontrol.data.DailyStat
import com.srproyecto.screencontrol.data.UsageRepository
import com.srproyecto.screencontrol.data.NotificationEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UsageRepository

    val allLimits: Flow<List<AppLimit>>
    val allNotifications: Flow<List<NotificationEntry>>

    private val _usageList = MutableStateFlow<List<AppUsageInfo>>(emptyList())

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val filteredUsageList: StateFlow<List<AppUsageInfo>> = combine(_usageList, _selectedCategory) { stats, category ->
        if (category == "Todas") {
            stats
        } else {
            stats.filter { repository.getAppCategory(it.packageName) == category }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _historyState = MutableStateFlow<List<DailyStat>>(emptyList())
    val historyState: StateFlow<List<DailyStat>> = _historyState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.dao()
        repository = UsageRepository(application, dao)

        allLimits = repository.allLimits
        allNotifications = repository.allNotifications

        // Cargar datos iniciales
        loadUsageStats()
    }

    // Cambiar la categoría desde la UI
    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun loadHistory(days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val data = repository.getHistoryStatsWithCache(days)
                _historyState.value = data
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startTime = calendar.timeInMillis

                val stats = repository.getUsageStats(startTime, endTime)
                _usageList.value = stats
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveLimit(limit: AppLimit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveLimit(limit)
        }
    }

    fun deleteLimit(limit: AppLimit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLimit(limit)
        }
    }

    // -----------------------------------------------------

    private val _installedApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val installedApps: StateFlow<List<AppUsageInfo>> = _installedApps

    fun loadInstalledApps(packageManager: PackageManager) {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 }
                .map { appInfo ->
                    AppUsageInfo(
                        packageName = appInfo.packageName,
                        appName = packageManager.getApplicationLabel(appInfo).toString(),
                        usageTimeMillis = 0L
                    )
                }.sortedBy { it.appName }
            _installedApps.value = apps
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotifications()
        }
    }

}
package com.example.healthmonitor.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmonitor.data.DataRepository
import com.example.healthmonitor.data.HealthData
import com.example.healthmonitor.ui.main.MainScreenUiState.Success
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.launch

class MainScreenViewModel(private val dataRepository: DataRepository) : ViewModel() {
  val uiState: StateFlow<MainScreenUiState> =
    dataRepository.data
      .map<HealthData?, MainScreenUiState> { data -> 
          if (data == null) MainScreenUiState.Empty else MainScreenUiState.Success(data)
      }
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Empty)

  fun updateVitals(heartRate: Int, systolicBp: Int, diastolicBp: Int, steps: Int, sleepHours: Int, sleepMinutes: Int) {
      viewModelScope.launch {
          dataRepository.updateData(heartRate, systolicBp, diastolicBp, steps, sleepHours, sleepMinutes)
      }
  }
}

sealed interface MainScreenUiState {
  object Empty : MainScreenUiState
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val data: HealthData) : MainScreenUiState
}

package com.example.weatherforecast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    var uiState: ForecastUiState by mutableStateOf(ForecastUiState.Loading)
        private set

    private val _savedCities = mutableStateListOf<City>()
    val savedCities: List<City> get() = _savedCities

    fun addCity(cityName: String) {
        viewModelScope.launch {
            uiState = ForecastUiState.Loading
            try {
                val city = ApiClient.getCityCoordinates(cityName)
                if (city != null) {
                    _savedCities.add(city)
                    loadForecast(city)
                } else {
                    uiState = ForecastUiState.Error("Город не найден")
                }
            } catch (e: Exception) {
                uiState = ForecastUiState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun loadForecast(city: City) {
        viewModelScope.launch {
            uiState = ForecastUiState.Loading
            try {
                val response = ApiClient.getWeatherForecast(city.latitude, city.longitude)
                val hourlyList = response.hourly.time.zip(response.hourly.temperature)
                uiState = ForecastUiState.Success(city.name, hourlyList)
            } catch (e: Exception) {
                uiState = ForecastUiState.Error("Ошибка загрузки прогноза")
            }
        }
    }
}
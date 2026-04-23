package com.example.weatherforecast

// Классы
data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String
)

data class WeatherResponse(
    val hourly: HourlyData
)

data class HourlyData(
    val time: List<String>,
    val temperature: List<Double>,
    val weatherCode: List<Int>
)

// Интерфейс
sealed interface ForecastUiState {
    object Loading : ForecastUiState
    data class Success(
        val cityName: String,
        val hourlyData: List<HourlyInfo>
    ) : ForecastUiState
    data class Error(val message: String) : ForecastUiState
}

data class HourlyInfo(
    val time: String,
    val temperature: Double,
    val weatherCode: Int
)
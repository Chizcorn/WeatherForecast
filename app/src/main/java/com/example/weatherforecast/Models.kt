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
    val temperature: List<Double>
)

// Интерфыейс
sealed interface ForecastUiState {
    object Loading : ForecastUiState
    data class Success(
        val cityName: String,
        val hourlyData: List<Pair<String, Double>>
    ) : ForecastUiState
    data class Error(val message: String) : ForecastUiState
}
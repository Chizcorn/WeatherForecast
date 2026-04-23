package com.example.weatherforecast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ApiClient {
    private const val NINJAS_KEY = "BDKc8kxwJxj45PKc0UN3EZ4oDFcXVqEZjtrMZ4Rj"

    suspend fun getCityCoordinates(cityName: String): City? = withContext(Dispatchers.IO) {
        val encodedName = URLEncoder.encode(cityName, "UTF-8")
        val url = URL("https://api.api-ninjas.com/v1/city?name=$encodedName")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("X-Api-Key", NINJAS_KEY)
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            val errorStream = connection.errorStream
            val errorMsg = errorStream?.bufferedReader()?.readText() ?: "Код $responseCode"
            throw Exception("Ошибка сервера: $errorMsg")
        }

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val jsonStr = reader.readText()
        reader.close()

        val jsonArray = JSONArray(jsonStr)
        if (jsonArray.length() == 0) return@withContext null

        val jsonObject = jsonArray.getJSONObject(0)
        City(
            name = jsonObject.getString("name"),
            latitude = jsonObject.getDouble("latitude"),
            longitude = jsonObject.getDouble("longitude"),
            country = jsonObject.getString("country")
        )
    }

    suspend fun getWeatherForecast(lat: Double, lon: Double): WeatherResponse = withContext(Dispatchers.IO) {
        val url = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&hourly=temperature_2m,weather_code&forecast_days=16")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val jsonStr = reader.readText()
        reader.close()

        val jsonObject = JSONObject(jsonStr)
        val hourlyObj = jsonObject.getJSONObject("hourly")

        val timeArray = hourlyObj.getJSONArray("time")
        val tempArray = hourlyObj.getJSONArray("temperature_2m")
        val weatherCodeArray = hourlyObj.getJSONArray("weather_code")

        val times = (0 until timeArray.length()).map { timeArray.getString(it) }
        val temps = (0 until tempArray.length()).map { tempArray.getDouble(it) }
        val codes = (0 until weatherCodeArray.length()).map { weatherCodeArray.getInt(it) }

        WeatherResponse(
            hourly = HourlyData(
                time = times,
                temperature = temps,
                weatherCode = codes
            )
        )
    }
}
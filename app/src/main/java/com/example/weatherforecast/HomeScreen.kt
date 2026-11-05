package com.example.weatherforecast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(viewModel: WeatherViewModel = viewModel()) {
    var cityInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Прогноз погоды",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                label = { Text("Город") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (cityInput.isNotBlank()) {
                    viewModel.addCity(cityInput.trim())
                    cityInput = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Добавить")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))


        if (viewModel.savedCities.isNotEmpty()) {
            Text(
                text = "Сохранённые города:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                items(viewModel.savedCities.size) { index ->
                    val city = viewModel.savedCities[index]
                    TextButton(
                        onClick = { viewModel.loadForecast(city) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${city.name}, ${city.country}")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (val state = viewModel.uiState) {
            is ForecastUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ForecastUiState.Success -> {
                Text(
                    text = "Прогноз для ${state.cityName}:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(state.hourlyData.take(24).size) { index ->
                        WeatherHourRow(state.hourlyData[index])
                    }
                }
            }
            is ForecastUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherHourRow(hourInfo: HourlyInfo) {
    val displayTime = hourInfo.time.substringAfter("T")
    val weatherIcon = weatherCodeToIcon(hourInfo.weatherCode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayTime,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = weatherIcon,
                contentDescription = "Погода",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${hourInfo.temperature}°C",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}
fun weatherCodeToIcon(code: Int): ImageVector {
    return when (code) {
        0 -> Icons.Default.WbSunny          // ясно
        1, 2, 3 -> Icons.Default.Cloud      // переменная облачность, пасмурно
        45, 48 -> Icons.Default.WaterDrop       // туман
        51, 53, 55 -> Icons.Default.WaterDrop // морось
        61, 63, 65 -> Icons.Default.Umbrella // дождь
        71, 73, 75 -> Icons.Default.AcUnit  // снег
        80, 81, 82 -> Icons.Default.Umbrella // ливень
        95, 96, 99 -> Icons.Default.Thunderstorm // гроза
        else -> Icons.Default.Cloud         // по умолчанию облака
    }
}
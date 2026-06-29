package com.example.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertLog
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val isConnected by viewModel.isEsp32Connected.collectAsState()
    val hr by viewModel.liveHeartRate.collectAsState()
    val spo2 by viewModel.liveSpo2.collectAsState()
    val temp by viewModel.liveTemperature.collectAsState()
    val lat by viewModel.liveLatitude.collectAsState()
    val lng by viewModel.liveLongitude.collectAsState()
    val lastTx by viewModel.lastTransmissionTime.collectAsState()
    val alerts by viewModel.alertLogs.collectAsState()

    var showSimulator by remember { mutableStateOf(false) }

    // Simulator slider states
    var simHr by remember { mutableStateOf(76f) }
    var simSpo2 by remember { mutableStateOf(98f) }
    var simTemp by remember { mutableStateOf(36.6f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            HeaderSection()
        }

        // Connection Banner & Alert Banner
        item {
            ConnectionBanner(
                isConnected = isConnected,
                lastTx = lastTx,
                onToggleConnection = { viewModel.toggleEsp32Connection() }
            )
        }

        // Active critical alerts
        val activeAlerts = alerts.filter { !it.resolved }
        if (activeAlerts.isNotEmpty()) {
            items(activeAlerts) { alert ->
                EmergencyAlertCard(
                    alert = alert,
                    onResolve = { viewModel.resolveAlert(alert.id) }
                )
            }
        }

        // Live Vitals Status Grid
        item {
            Text(
                text = "நேரடி உடல்நிலை கண்காணிப்பு (Live Vitals)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            VitalsGrid(hr = hr, spo2 = spo2, temp = temp)
        }

        // GPS Location tracking status
        item {
            LocationTrackingCard(lat = lat, lng = lng)
        }

        // Hardware simulation section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = "Simulate", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ESP32 வன்பொருள் சிமுலேட்டர் (Simulator)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            modifier = Modifier.testTag("toggle_simulator_button"),
                            onClick = { showSimulator = !showSimulator }
                        ) {
                            Icon(
                                imageVector = if (showSimulator) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand simulator"
                            )
                        }
                    }

                    if (showSimulator) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "வழிகாட்டி: ESP32 Wearable-இன் நிகழ்நேர சிக்னல்களை மாற்ற கீழே உள்ள ஸ்லைடர்களை நகர்த்தி 'டேட்டா அனுப்பு' என்பதைக் கிளிக் செய்க.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Heart Rate Slider
                        Text(text = "இதயத் துடிப்பு (Heart Rate): ${simHr.toInt()} BPM", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = simHr,
                            onValueChange = { simHr = it },
                            valueRange = 40f..160f,
                            modifier = Modifier.testTag("slider_hr")
                        )

                        // SpO2 Slider
                        Text(text = "ஆக்சிஜன் அளவு (SpO2): ${simSpo2.toInt()}%", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = simSpo2,
                            onValueChange = { simSpo2 = it },
                            valueRange = 80f..100f,
                            modifier = Modifier.testTag("slider_spo2")
                        )

                        // Temperature Slider
                        Text(text = "வெப்பநிலை (Temperature): ${"%.1f".format(simTemp)}°C", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = simTemp,
                            onValueChange = { simTemp = it },
                            valueRange = 34.0f..41.0f,
                            modifier = Modifier.testTag("slider_temp")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.sendMockSensorPacket(simHr.toInt(), simSpo2.toInt(), simTemp, lat, lng)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("transmit_packet_button")
                            ) {
                                Text("டேட்டா அனுப்பு (Send Packet)")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.simulateFall() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulate_fall_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("விழுதல் (Simulate Fall)", fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = { viewModel.triggerSos() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulate_sos_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SOS பொத்தான் (Trigger SOS)", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Historical Activity/Alert log history title
        item {
            Text(
                text = "அபாய எச்சரிக்கை வரலாறு (Alert Log History)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val resolvedAlerts = alerts.filter { it.resolved }
        if (resolvedAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "முந்தைய எச்சரிக்கை பதிவுகள் எதுவும் இல்லை.\n(No historical alerts logged yet)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(resolvedAlerts) { alert ->
                ResolvedAlertItem(alert = alert)
            }
            item {
                OutlinedButton(
                    onClick = { viewModel.clearAllAlertLogs() },
                    modifier = Modifier.fillMaxWidth().testTag("clear_logs_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("வரலாற்றை அழி (Clear Alert History)")
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "உயிர் காவலன்",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "UYIR KAAVALAN - Guardian of Life",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        Divider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun ConnectionBanner(
    isConnected: Boolean,
    lastTx: Long,
    onToggleConnection: () -> Unit
) {
    val df = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    val formattedTime = df.format(Date(lastTx))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (isConnected) Color(0xFF00C853) else Color.Red,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isConnected) "ESP32 அணியக்கூடிய சாதனம் இணைக்கப்பட்டுள்ளது" else "சாதனம் துண்டிக்கப்பட்டுள்ளது",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "கடைசி சிக்னல் (Last Packet): $formattedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
            OutlinedButton(
                onClick = onToggleConnection,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.testTag("toggle_connection_button")
            ) {
                Text(if (isConnected) "Disconnect" else "Connect", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EmergencyAlertCard(
    alert: AlertLog,
    onResolve: () -> Unit
) {
    // Flashing Animation for Emergency Alert
    val infiniteTransition = rememberInfiniteTransition(label = "flash")
    val animatedBgColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.errorContainer,
        targetValue = MaterialTheme.colorScheme.error,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash_bg"
    )

    val contentColor = if (animatedBgColor == MaterialTheme.colorScheme.error) Color.White else MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Danger Alarm",
                    tint = contentColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "அபாய எச்சரிக்கை! (EMERGENCY)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = alert.alertMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "இடம் (GPS): ${"%.4f".format(alert.latitude)}, ${"%.4f".format(alert.longitude)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = onResolve,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("resolve_alert_${alert.id}")
                ) {
                    Text("நான் பாதுகாப்பாக உள்ளேன் (I'm Safe)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VitalsGrid(hr: Int, spo2: Int, temp: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Heart Rate Card
        VitalCard(
            title = "இதயத் துடிப்பு",
            subTitle = "Heart Rate",
            value = if (hr == 0) "--" else "$hr",
            unit = "BPM",
            status = when {
                hr == 0 -> "No Signal"
                hr < 55 -> "Low Pulse"
                hr > 110 -> "High Pulse"
                else -> "Normal"
            },
            statusColor = when {
                hr == 0 -> Color.Gray
                hr < 55 || hr > 110 -> MaterialTheme.colorScheme.error
                else -> Color(0xFF00C853)
            },
            icon = Icons.Default.Favorite,
            iconTint = Color.Red,
            pulseIcon = hr > 0,
            modifier = Modifier.weight(1f)
        )

        // SpO2 Card
        VitalCard(
            title = "ஆக்சிஜன் அளவு",
            subTitle = "SpO2 Saturation",
            value = if (spo2 == 0) "--" else "$spo2",
            unit = "%",
            status = when {
                spo2 == 0 -> "No Signal"
                spo2 < 93 -> "Hypoxia Warning"
                else -> "Normal"
            },
            statusColor = when {
                spo2 == 0 -> Color.Gray
                spo2 < 93 -> MaterialTheme.colorScheme.error
                else -> Color(0xFF00C853)
            },
            icon = Icons.Default.Refresh,
            iconTint = Color(0xFF0288D1),
            pulseIcon = false,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Temperature Card
        VitalCard(
            title = "வெப்பநிலை",
            subTitle = "Temperature",
            value = if (temp == 0f) "--" else "${"%.1f".format(temp)}",
            unit = "°C",
            status = when {
                temp == 0f -> "No Signal"
                temp > 38.2f -> "High Fever"
                temp < 35.0f -> "Hypothermia"
                else -> "Normal"
            },
            statusColor = when {
                temp == 0f -> Color.Gray
                temp > 38.2f || temp < 35.0f -> MaterialTheme.colorScheme.error
                else -> Color(0xFF00C853)
            },
            icon = Icons.Default.Info,
            iconTint = Color(0xFFF57C00),
            pulseIcon = false,
            modifier = Modifier.weight(1f)
        )

        // Empty spacer / aesthetic placeholder to keep grid proportional
        Card(
            modifier = Modifier
                .weight(1f)
                .height(150.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "பாதுகாப்பான வலையம்",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Safe Zone Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VitalCard(
    title: String,
    subTitle: String,
    value: String,
    unit: String,
    status: String,
    statusColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    pulseIcon: Boolean,
    modifier: Modifier = Modifier
) {
    // Pulse animation for heart icon
    val scale = if (pulseIcon) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val animatedScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        animatedScale
    } else {
        1.0f
    }

    Card(
        modifier = modifier.height(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subTitle, fontSize = 9.sp, color = Color.Gray)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scale)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LocationTrackingCard(lat: Double, lng: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "GPS",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "நேரடி இடப்பிடிப்பு (GPS Location Status)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Latitude: ${"%.5f".format(lat)} | Longitude: ${"%.5f".format(lng)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "மண்டலம்: சென்னை பெருநகரம் (Zone: Chennai Safe Area)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ResolvedAlertItem(alert: AlertLog) {
    val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = df.format(Date(alert.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Resolved",
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = alert.alertMessage,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "வகை: ${alert.alertType} | நேரம்: $timeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF00C853).copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "தீர்க்கப்பட்டது",
                    color = Color(0xFF00C853),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

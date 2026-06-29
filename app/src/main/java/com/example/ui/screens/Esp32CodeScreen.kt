package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Esp32ModelCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Esp32CodeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val verticalScroll = rememberScrollState()
    val horizontalScrollCode = rememberScrollState()
    val horizontalScrollWiring = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(verticalScroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hardware Card Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ESP32 வன்பொருள் மாதிரி குறியீடு",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "ESP32 Firmware Code & Schematic",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Introduction
        Text(
            text = "உயிர் காவலன் வன்பொருள் மாதிரி (ESP32 Wearable Model)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "உயிர் காவலாளன் அணியக்கூடிய சாதனத்தை உருவாக்க, MAX30102 (இதயத் துடிப்பு & ஆக்ஸிஜன் அளவு), MPU6050 (விழுதல் கண்டறிதல்), மற்றும் DS18B20 (வெப்பநிலை) சென்சார்களை ESP32 மைக்ரோகண்ட்ரோலருடன் இணைக்க வேண்டும். கீழே உள்ள திட்ட வரைபடம் மற்றும் அனலாக் மென்பொருள் குறியீட்டை உங்கள் பள்ளி ஆய்வக திட்டத்திற்கு பயன்படுத்தலாம்.",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )

        // Wiring Diagram block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. மின்சுற்று வரைபடம் (Wiring Connections)",
                        color = Color(0xFF4FC3F7),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(
                        onClick = {
                            copyToClipboard(context, Esp32ModelCode.WIRING_DIAGRAM, "Schematic copied!")
                        },
                        modifier = Modifier.testTag("copy_schematic_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Schematic", tint = Color.LightGray)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.horizontalScroll(horizontalScrollWiring)) {
                    Text(
                        text = Esp32ModelCode.WIRING_DIAGRAM.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE0E0E0),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Arduino C++ Code block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. அர்டுயினோ மென்பொருள் குறியீடு (Arduino C++ Source)",
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            copyToClipboard(context, Esp32ModelCode.ARDUINO_C_CODE, "Arduino sketch copied!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.testTag("copy_arduino_code_button")
                    ) {
                        Text("Copy Code", fontSize = 11.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.horizontalScroll(horizontalScrollCode)) {
                    Text(
                        text = Esp32ModelCode.ARDUINO_C_CODE,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Tips Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "பள்ளி திட்ட குறிப்புகள் (Science Project Tips):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Arduino IDE இல் Adafruit MPU6050, MAX30105 மற்றும் DallasTemperature நூலகங்களை (Libraries) நிறுவ மறக்காதீர்கள்.\n" +
                                "• ESP32 வைஃபை ஹாட்ஸ்பாட் அல்லது புளூடூத் சீரியல் மூலம் இந்த ஆண்ட்ராய்டு பயன்பாட்டுடன் இணைக்க முடியும்.\n" +
                                "• விழுதல் அல்லது அவசர SOS பொத்தான் அழுத்தப்படும்போது, ஆண்ட்ராய்டு செயலியானது தானாகவே caregiver-க்கு அபாய அறிவிப்புகளை வெளியிடுகிறது.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Uyir Kaavalan Code", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

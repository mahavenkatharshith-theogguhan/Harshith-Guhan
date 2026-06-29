package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Instantiates our Room-backed ViewModel
                val viewModel: HealthViewModel = viewModel()
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Favorite, contentDescription = "Dashboard") },
                                label = { Text("கண்காணிப்பு", fontSize = 10.sp) }, // Vitals / Monitor
                                modifier = Modifier.testTag("nav_tab_dashboard")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Info, contentDescription = "AI Predictions") },
                                label = { Text("AI கணிப்பு", fontSize = 10.sp) }, // AI Prediction
                                modifier = Modifier.testTag("nav_tab_predictions")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.Face, contentDescription = "Voice Assistant") },
                                label = { Text("உதவியாளர்", fontSize = 10.sp) }, // Assistant
                                modifier = Modifier.testTag("nav_tab_assistant")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Medications") },
                                label = { Text("மாத்திரைகள்", fontSize = 10.sp) }, // Medications
                                modifier = Modifier.testTag("nav_tab_medications")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                icon = { Icon(Icons.Default.Build, contentDescription = "ESP32 Hardware") },
                                label = { Text("வன்பொருள்", fontSize = 10.sp) }, // Hardware Code
                                modifier = Modifier.testTag("nav_tab_hardware")
                            )
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    when (selectedTab) {
                        0 -> DashboardScreen(viewModel = viewModel, modifier = modifier)
                        1 -> PredictionsScreen(viewModel = viewModel, modifier = modifier)
                        2 -> AssistantScreen(viewModel = viewModel, modifier = modifier)
                        3 -> MedicationScreen(viewModel = viewModel, modifier = modifier)
                        4 -> Esp32CodeScreen(modifier = modifier)
                    }
                }
            }
        }
    }
}

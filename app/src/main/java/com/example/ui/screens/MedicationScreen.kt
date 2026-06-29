package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicationReminder
import com.example.ui.viewmodel.HealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.medicationReminders.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medTime by remember { mutableStateOf("08:00 AM") }

    val presetTimes = listOf("08:00 AM", "01:00 PM", "08:30 PM")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Medication Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "மருந்து மற்றும் மாத்திரை நினைவூட்டல்",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Medication & Pill Reminders",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Add Reminder button / Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!showAddForm) {
                        Button(
                            onClick = { showAddForm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expand_add_medication_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("மாத்திரை நினைவூட்டலைச் சேர் (Add Pill Reminder)")
                        }
                    } else {
                        Text(
                            text = "புதிய மாத்திரை நினைவூட்டல் (New Medication)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = medName,
                            onValueChange = { medName = it },
                            label = { Text("மாத்திரையின் பெயர் (Medicine Name)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_med_name"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = medDosage,
                            onValueChange = { medDosage = it },
                            label = { Text("அளவு (Dosage - e.g., 500mg, 1 tablet)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_med_dosage"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "நினைவூட்டல் நேரம் (Select Time):", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetTimes.forEach { time ->
                                FilterChip(
                                    selected = (medTime == time),
                                    onClick = { medTime = time },
                                    label = { Text(time) },
                                    modifier = Modifier.testTag("time_chip_$time")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddForm = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cancel_add_med_button")
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    if (medName.isNotBlank() && medDosage.isNotBlank()) {
                                        viewModel.addMedication(medName, medDosage, medTime)
                                        medName = ""
                                        medDosage = ""
                                        showAddForm = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_med_button")
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }

        // Active Reminders List Title
        item {
            Text(
                text = "இன்றைய மாத்திரைகள் (Today's Pills)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (reminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "மாத்திரைகள் எதுவும் இல்லை.\n(No medication reminders set yet.)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(reminders) { reminder ->
                MedicationItemCard(
                    reminder = reminder,
                    onToggleTaken = { viewModel.toggleMedication(reminder) },
                    onDelete = { viewModel.deleteMedication(reminder) }
                )
            }
        }
    }
}

@Composable
fun MedicationItemCard(
    reminder: MedicationReminder,
    onToggleTaken: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("med_item_${reminder.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isTakenToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (reminder.isTakenToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = reminder.isTakenToday,
                    onCheckedChange = { onToggleTaken() },
                    modifier = Modifier.testTag("med_checkbox_${reminder.id}")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = reminder.medicineName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (reminder.isTakenToday) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (reminder.isTakenToday) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "அளவு (Dosage): ${reminder.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = reminder.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (reminder.isTakenToday) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00C853),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "எடுத்துக் கொள்ளப்பட்டது (Taken)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF00C853),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_med_button_${reminder.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete medication reminder",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

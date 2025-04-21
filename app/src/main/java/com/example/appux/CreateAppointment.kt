package com.example.appux

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Timer
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CreateAppointment(
    navController: NavController,
    user: User,
    database: AppDatabase
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedService by remember { mutableStateOf("") }
    var specifications by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            1 -> ServiceSelectionStep(
                selectedService = selectedService,
                onServiceSelected = { selectedService = it },
                specifications = specifications,
                onSpecificationsChanged = { specifications = it },
                onNextClicked = { currentStep = 2 },
                navController
            )
            2 -> DateTimeSelectionStep(
                onDateTimeChanged = { dateTime ->
                    selectedDateTime = dateTime
                },
                onNextClicked = { currentStep = 3 },
                onBackClicked = { currentStep = 1 }
            )
            3 -> ConfirmationStep(
                selectedService = selectedService,
                specifications = specifications,
                dateTime = selectedDateTime,
                onConfirmClicked = {
                    scope.launch {
                        val newAppointment = Appointment(
                            userId = user.id,
                            userName = user.name,
                            serviceType = selectedService,
                            specifications = specifications,
                            dateTime = selectedDateTime
                        )
                        database.appointmentDao().insertAppointment(newAppointment)
                    }
                    navController.popBackStack()
                },
                onBackClicked = { currentStep = 2 }
            )
        }
    }
}

@Composable
fun ServiceSelectionStep(
    selectedService: String,
    onServiceSelected: (String) -> Unit,
    specifications: String,
    onSpecificationsChanged: (String) -> Unit,
    onNextClicked: () -> Unit,
    navController: NavController
) {
    val serviceOptions = listOf("Haircut", "Beard Trim", "Hair Dye", "Hair Treatment")
    val defaultColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Color grisáceo
    val selectedColor = MaterialTheme.colorScheme.primary

    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select a service", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedService.isEmpty()) defaultColor else selectedColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedService.ifEmpty { "Select a Service" },
                        modifier = Modifier.weight(1f),
                        color = if (selectedService.isEmpty()) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Open dropdown menu",
                        tint = LocalContentColor.current.copy(alpha = 0.38f)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                serviceOptions.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service) },
                        onClick = {
                            onServiceSelected(service)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = specifications,
            onValueChange = onSpecificationsChanged,
            label = { Text("Specifications") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Back")
            }
            Button(
                onClick = onNextClicked,
                enabled = selectedService.isNotEmpty() // Enable next only if a service is selected
            ) {
                Text(text = "Next")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    showDialog: Boolean,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DateTimeSelectionStep(
    onDateTimeChanged: (String) -> Unit,
    onNextClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    var showDatePickerModal by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) } // Include year
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val defaultColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val selectedColor = MaterialTheme.colorScheme.primary

    val isDateTimeSelected by remember(selectedDate, selectedTime) {
        derivedStateOf { selectedDate != null && selectedTime != null }
    }

    LaunchedEffect(selectedDate, selectedTime) {
        if (selectedDate != null && selectedTime != null) {
            val date = Date(selectedDate!!)
            val formattedDate = dateFormatter.format(date)
            val formattedTime = timeFormatter.format(selectedTime!!)
            onDateTimeChanged("$formattedDate $formattedTime")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Enter Date and Time", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showDatePickerModal = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedDate == null) defaultColor else selectedColor
            )
        ) {
            Text(
                text = if (selectedDate != null) {
                    "Date: ${dateFormatter.format(Date(selectedDate!!))}"
                } else {
                    "Select Date"
                },
                color = if (selectedDate == null) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = "Calendar",
                tint = if (selectedDate == null) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current
            )
        }

        DatePickerModal(
            showDialog = showDatePickerModal,
            onDateSelected = {
                selectedDate = it
                showDatePickerModal = false
            },
            onDismiss = { showDatePickerModal = false }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { showTimePickerDialog = !showTimePickerDialog },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTime == null) defaultColor else selectedColor
            )
        ) {
            Text(
                text = if (selectedTime != null) {
                    "Time: ${timeFormatter.format(selectedTime!!)}"
                } else {
                    "Select Time"
                },
                color = if (selectedTime == null) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Filled.Timer, contentDescription = "Time",
                tint = if (selectedTime == null) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current
            )
        }

        AnimatedVisibility(showTimePickerDialog) {
            Dial(
                onTimeSelected = { localTime ->
                    selectedTime = localTime
                    showTimePickerDialog = false
                },
                onDismiss = { showTimePickerDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBackClicked) {
                Text(text = "Back")
            }
            Button(
                onClick = onNextClicked,
                enabled = isDateTimeSelected
            ) {
                Text(text = "Next")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dial(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimePicker(
            state = timePickerState,
        )
        Button(onClick = {
            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            onDismiss()
        }) {
            Text("Confirm Time")
        }
    }
}

@Composable
fun ConfirmationStep(
    selectedService: String,
    specifications: String,
    dateTime: String,
    onConfirmClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Confirmation", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Service: $selectedService")
        Text(text = "Specifications: $specifications")
        Text(text = "Date and Time: $dateTime")

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBackClicked) {
                Text(text = "Back")
            }
            Button(onClick = onConfirmClicked) {
                Text(text = "Confirm")
            }
        }
    }
}
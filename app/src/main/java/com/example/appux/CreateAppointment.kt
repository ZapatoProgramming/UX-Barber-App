package com.example.appux

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
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
    database: AppDatabase,
    initialServices: List<String> = emptyList(), // Servicios seleccionados inicialmente (por defecto, lista vacía)
    initialSpecifications: String = "" // Especificaciones iniciales (por defecto, cadena vacía)
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedServices by remember { mutableStateOf(initialServices) } // Inicializa con initialServices
    var specifications by remember { mutableStateOf(initialSpecifications) } // Inicializa con initialSpecifications
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
                selectedServices = selectedServices,
                onServicesSelected = { selectedServices = it },
                specifications = specifications,
                onSpecificationsChanged = { newSpecifications -> specifications = newSpecifications },
                onNextClicked = { currentStep = 2 },
                navController = navController
            )
            2 -> DateTimeSelectionStep(
                onDateTimeChanged = { dateTime -> selectedDateTime = dateTime },
                onNextClicked = { currentStep = 3 },
                onBackClicked = { currentStep = 1 }
            )
            3 -> ConfirmationStep(
                selectedServices = selectedServices,
                specifications = specifications,
                dateTime = selectedDateTime,
                onConfirmClicked = {
                    scope.launch {
                        val newAppointment = Appointment(
                            userId = user.id,
                            userName = user.name,
                            serviceType = selectedServices.joinToString(", "), // Combina los servicios seleccionados
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
    selectedServices: List<String>, // Lista de servicios seleccionados inicialmente
    onServicesSelected: (List<String>) -> Unit, // Callback para notificar cambios en los servicios seleccionados
    specifications: String, // Especificaciones iniciales
    onSpecificationsChanged: (String) -> Unit, // Callback para notificar cambios en las especificaciones
    onNextClicked: () -> Unit, // Acción al hacer clic en "Next"
    navController: NavController // Controlador de navegación
) {
    val serviceOptions = listOf("Haircut", "Beard Trim", "Hair Dye", "Hair Treatment")
    val defaultColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Color grisáceo
    val selectedColor = MaterialTheme.colorScheme.primary

    // Estado para los servicios seleccionados
    var selectedServicesState by remember { mutableStateOf(selectedServices.toSet()) }

    fun updateSelectedServices(newService: String, isChecked: Boolean) {
        val updatedServices = if (isChecked) {
            selectedServicesState + newService // Agregar el servicio
        } else {
            selectedServicesState - newService // Eliminar el servicio
        }
        selectedServicesState = updatedServices // Actualizar el estado con una nueva colección inmutable
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select services",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Lista de checkboxes para los servicios
        serviceOptions.forEach { service ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = selectedServicesState.contains(service),
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            updateSelectedServices(service, true)
                        } else {
                            updateSelectedServices(service, false)
                        }
                        onServicesSelected(selectedServicesState.toList()) // Notifica cambios al padre
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = selectedColor,
                        uncheckedColor = defaultColor
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = service,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = specifications,
            onValueChange = onSpecificationsChanged, // Notifica cambios en las especificaciones
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
                enabled = selectedServicesState.isNotEmpty() // Habilitar "Next" solo si hay servicios seleccionados
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
    selectedServices: List<String>, // Lista de servicios seleccionados
    specifications: String, // Especificaciones ingresadas por el usuario
    dateTime: String, // Fecha y hora seleccionada
    onConfirmClicked: () -> Unit, // Acción al hacer clic en "Confirm"
    onBackClicked: () -> Unit // Acción al hacer clic en "Back"
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Confirmation",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar los servicios seleccionados
        Text(
            text = "Services:",
            style = MaterialTheme.typography.titleMedium
        )
        selectedServices.forEach { service ->
            Text(
                text = "- $service",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Specifications: $specifications",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Date and Time: $dateTime",
            style = MaterialTheme.typography.bodyMedium
        )

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
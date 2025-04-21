package com.example.appux

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.google.gson.Gson


@Composable
fun Home(navController: NavController, user: User, database: AppDatabase) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if(user.isBarber) {
            HomeBarber(navController, user, database)
        }
        else{
            HomeClient(navController, user, database)
        }
    }
}

@Composable
fun HomeClient(navController: NavController, user: User, database: AppDatabase) {
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    LaunchedEffect(Unit) {
        val dbAppointments = database.appointmentDao().getAppointmentsByUserId(user.id)
        appointments = dbAppointments
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Welcome ${user.name}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (appointments.isEmpty()) {
            Text(
                text = "There are no appointments scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments.size) { appointment ->
                    AppointmentCard(appointment = appointments[appointment])
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val userJson = Gson().toJson(user)
                navController.navigate("create_appointment/$userJson")
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "+")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("login") }) {
            Text(text = "Cerrar Sesión")
        }
    }

}

@Composable
fun HomeBarber(navController: NavController, user: User, database: AppDatabase) {
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    LaunchedEffect(Unit) {
        val dbAppointments = database.appointmentDao().getAppointments()
        appointments = dbAppointments
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Welcome Barber ${user.name}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (appointments.isEmpty()) {
            Text(
                text = "There are no appointments scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments.size) { appointment ->
                    AppointmentCard(appointment = appointments[appointment], forBarber = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("login") }) {
            Text(text = "Log Out")
        }
    }

}

@Composable
fun AppointmentCard(appointment: Appointment, forBarber: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if(forBarber){
                Text(
                    text = "Client: ${appointment.userName}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "Service: ${appointment.serviceType}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Specifications: ${appointment.specifications}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Date and Time: ${appointment.dateTime}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
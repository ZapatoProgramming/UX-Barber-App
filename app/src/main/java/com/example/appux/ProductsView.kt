package com.example.appux

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun ProductsView(navController: NavController) {
    val services = listOf(
        "Haircut" to Icons.Default.ContentCut,
        "Beard Trim" to Icons.Default.Face,
        "Hair Treatment" to Icons.Default.Spa,
        "Hair Dye" to Icons.Default.ColorLens
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(services.size) { index ->
            val (serviceName, icon) = services[index]
            ServiceCard(
                serviceName = serviceName,
                icon = icon,
                onClick = {
                    navController.navigate("service_details/$serviceName")
                }
            )
        }
    }
}

@Composable
fun ServiceCard(serviceName: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = serviceName, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ServiceDetailsView(navController: NavController, serviceName: String) {
    val serviceOptions: Map<String, Int?> = when (serviceName) {
        "Haircut" -> mapOf(
            "Buzz Cut" to R.drawable.buzz_cut,
            "Quiff" to R.drawable.quiff,
            "CR7" to R.drawable.cr7,
            "Shag" to R.drawable.shagg
        )
        "Beard Trim" -> mapOf(
            "Option A" to null,
            "Option B" to null,
            "Option C" to null,
            "Option D" to null
        )
        "Hair Treatment" -> mapOf(
            "Basic" to null,
            "Advanced" to null,
            "Premium" to null,
            "Luxury" to null
        )
        "Hair Dye" -> mapOf(
            "Blonde" to null,
            "Brunette" to null,
            "Red" to null,
            "Black" to null
        )
        else -> emptyMap()
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // BackButton
        BackButton(navController)

        // LazyVerticalGrid para mostrar las opciones
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(serviceOptions.keys.toList()) { option ->
                OptionCard(
                    optionName = option,
                    imageResId = serviceOptions[option],
                    onClick = {
                        if (serviceName == "Hair Dye") {
                            navController.navigate("hair_dye_details/$option")
                        } else {
                            selectedOption = option
                            showDialog = true
                        }
                    }
                )
            }
        }
        if (showDialog) {
            ConfirmationDialog(
                title = "Confirm Reservation",
                message = "Do you want to book the service '$serviceName' with specifications '$selectedOption'?",
                onConfirm = {
                    navController.navigate("create_appointment?selectedServices=$serviceName&specifications=$selectedOption")
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun HairDyeDetailsView(color: String, navController: NavController) {
    val productInfo = when (color) {
        "Blonde" -> "This is a light blonde hair dye. Perfect for a natural sun-kissed look."
        "Brunette" -> "This is a rich brunette hair dye. Ideal for adding depth and shine."
        "Red" -> "This is a vibrant red hair dye. Adds boldness and warmth to your style."
        "Black" -> "This is a deep black hair dye. Creates a sleek and sophisticated look."
        else -> "Product information not available."
    }

    var showDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        BackButton(navController)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = color, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = productInfo, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showDialog = true }) {
            Text("Reserve")
        }
    }

    if (showDialog) {
        ConfirmationDialog(
            title = "Confirm Reservation",
            message = "Do you want to book the service 'Hair Dye' with specifications '$color'?",
            onConfirm = {
                navController.navigate("create_appointment?selectedServices=Hair Dye&specifications=$color")
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun OptionCard(
    optionName: String,
    imageResId: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = optionName,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = optionName,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Yes")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

@Composable
fun BackButton(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start, // Alineación al inicio
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { navController.popBackStack() }, // Retrocede a la pantalla anterior
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
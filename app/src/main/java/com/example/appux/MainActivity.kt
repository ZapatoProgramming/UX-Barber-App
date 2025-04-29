package com.example.appux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appux.ui.theme.AppuxTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)

        setContent {
            AppuxTheme {
                val navController = rememberNavController()

                // Estado global para el usuario (se inicializa como null)
                var currentUser by remember { mutableStateOf<User?>(null) }

                // Define las rutas principales para la Bottom Navigation
                val bottomNavItems = listOf(
                    BottomNavItem("home", "Home", Icons.Default.Home),
                    BottomNavItem("products", "Products and Services", Icons.Default.Category),
                    BottomNavItem("profile", "Profile", Icons.Default.Person),
                )

                // Layout principal con Scaffold
                Scaffold(
                    bottomBar = {
                        // Mostrar la Bottom Navigation solo si el usuario ha iniciado sesión
                        if (currentUser != null) {
                            val showBottomBar = listOf("home", "profile", "settings").contains(
                                navController.currentBackStackEntry?.destination?.route
                            )
                            if (showBottomBar) {
                                NavigationBar {
                                    val backStackEntry by navController.currentBackStackEntryAsState()
                                    val currentRoute = backStackEntry?.destination?.route

                                    bottomNavItems.forEach { item ->
                                        NavigationBarItem(
                                            selected = currentRoute == item.route,
                                            onClick = {
                                                navController.navigate(item.route) {
                                                    launchSingleTop = true
                                                }
                                            },
                                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                                            label = { Text(text = item.title) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            Login(navController, database) { user ->
                                currentUser = user
                                navController.navigate("home")
                            }
                        }
                        composable("signup") {
                            SignUp(database) { user ->
                            currentUser = user
                            navController.navigate("home")
                        } }
                        composable("home") {
                            // Usar el usuario guardado en el estado global
                            Home(navController, currentUser!!, database)
                        }
                        composable(
                            "create_appointment?selectedServices={selectedServices}&specifications={specifications}",
                            arguments = listOf(
                                navArgument("selectedServices") {
                                    type = NavType.StringType
                                    defaultValue = "" // Valor predeterminado si no se pasa ningún argumento
                                },
                                navArgument("specifications") {
                                    type = NavType.StringType
                                    defaultValue = "" // Valor predeterminado si no se pasa ningún argumento
                                }
                            )
                        ) { backStackEntry ->
                            // Recuperar los argumentos pasados
                            val selectedServices = backStackEntry.arguments?.getString("selectedServices")
                                ?.split(",")
                                ?.filter { it.isNotEmpty() } // Convertir a lista y filtrar elementos vacíos
                                ?: emptyList()

                            val specifications = backStackEntry.arguments?.getString("specifications") ?: ""

                            // Llamar a CreateAppointment con los valores recuperados
                            CreateAppointment(
                                navController = navController,
                                user = currentUser!!,
                                database = database,
                                initialServices = selectedServices,
                                initialSpecifications = specifications
                            )
                        }
                        composable("profile") {
                            ProfileView(navController = navController, currentUser!!, database)
                        }
                        composable("settings") {
                            SettingsView()
                        }
                        composable("products") {
                            ProductsView(navController)
                        }

                        composable(
                            "service_details/{serviceName}",
                            arguments = listOf(navArgument("serviceName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
                            ServiceDetailsView(navController, serviceName)
                        }

                        composable(
                            "hair_dye_details/{color}",
                            arguments = listOf(navArgument("color") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val color = backStackEntry.arguments?.getString("color") ?: ""
                            HairDyeDetailsView(color, navController)
                        }
                    }
                }
            }
        }
    }
}

// Clase para representar los ítems de la Bottom Navigation
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)
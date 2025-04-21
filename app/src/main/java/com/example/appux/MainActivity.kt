package com.example.appux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appux.ui.theme.AppuxTheme
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        setContent {
            AppuxTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { Login(navController, database) }
                    composable("signup") { SignUp(navController, database) }
                    composable(
                        route = "home/{user}",
                        arguments = listOf(navArgument("user") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userJson = backStackEntry.arguments?.getString("user")
                        val user = Gson().fromJson(userJson, User::class.java)
                        Home(navController, user, database)
                    }
                    composable(
                        route = "create_appointment/{user}",
                        arguments = listOf(navArgument("user") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userJson = backStackEntry.arguments?.getString("user")
                        val user = Gson().fromJson(userJson, User::class.java)
                        CreateAppointment(navController, user, database)
                    }
                }
            }
        }
    }
}
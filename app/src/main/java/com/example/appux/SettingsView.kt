package com.example.appux

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsView(){
    Text("Settings")
}

@Preview(showBackground = true)
@Composable
fun SettingsViewPreview() {
    SettingsView()
}
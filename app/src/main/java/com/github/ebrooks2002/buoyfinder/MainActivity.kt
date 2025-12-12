package com.github.ebrooks2002.buoyfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ebrooks2002.buoyfinder.ui.theme.BuoyFinderTheme
import com.github.ebrooks2002.buoyfinder.ui.BuoyFinderApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuoyFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    BuoyFinderApp()
                }
            }
        }
    }
}

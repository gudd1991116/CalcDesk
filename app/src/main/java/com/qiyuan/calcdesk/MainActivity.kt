package com.qiyuan.calcdesk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qiyuan.calcdesk.navigation.CalcDeskNavHost
import com.qiyuan.calcdesk.ui.theme.CalcDeskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalcDeskTheme {
                CalcDeskNavHost()
            }
        }
    }
}

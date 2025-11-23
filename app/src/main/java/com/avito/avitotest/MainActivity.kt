package com.avito.avitotest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.avito.ui.theme.AvitoTestTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appComponent = (application as AvitoApplication).appComponent
        val isAuthorized = FirebaseAuth.getInstance().currentUser != null

        setContent {
            AvitoTestTheme {
                RootAppScreen(
                    isAuthorized = isAuthorized,
                    appComponent = appComponent
                )
            }
        }
    }
}

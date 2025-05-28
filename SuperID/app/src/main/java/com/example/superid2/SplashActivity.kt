package com.example.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.superid2.ui.theme.SuperID2Theme

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import com.example.superid2.descricaoActivity
import com.example.superid2.loginActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperID2Theme {
                SplashScreen()
            }
        }

        // Redireciona após 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, descricaoActivity::class.java))
            finish()
        }, 2000) // tempo da splash

    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(250.dp)
        )
    }
}

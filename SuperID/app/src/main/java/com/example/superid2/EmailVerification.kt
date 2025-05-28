package com.example.superid2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.R
import com.google.firebase.auth.FirebaseAuth
//Tela para o usuario aceitar os termos do app
class EmailVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmailVerificationScreen()
        }
    }
}
@Composable
fun EmailVerificationScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser //usuario que acabou de logar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo do App",
            modifier = Modifier
                .size(350.dp)
                .padding(bottom = 10.dp)
        )

        Text("Por favor verifique sua caixa de entrada do email",
            color = Color.White)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                user?.reload()?.addOnCompleteListener { task -> //verificacao para ver se usuario clicou no link de verificacao
                    if (user.isEmailVerified) {
                        Toast.makeText(context, "Email verificado com sucesso!", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, QrScannerActivity::class.java)) // Agora abre o QR
                        if (context is Activity) context.finish()
                    } else {
                        Toast.makeText(context, "Seu e-mail ainda não foi verificado.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1B5E20),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Já verifiquei o e-mail")
        }

    }
}


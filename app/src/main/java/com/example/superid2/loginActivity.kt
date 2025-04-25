package com.example.superid2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.R
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.collection.SparseArrayCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.superid2.TermosPopup
import java.text.Normalizer

class loginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            SuperID2Theme {
                LoginApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginApp() {
    LoginWithButton(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .wrapContentSize(Alignment.Center)
    )
}


@Composable
fun LoginWithButton(modifier: Modifier = Modifier) {
    var mostrarPopup by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    val verde = Color(0xFF1B5E20)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        // Conteúdo da tela de login
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo do App",
                modifier = Modifier
                    .size(350.dp)
                    .padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                textStyle = TextStyle(color = Color.White),
                label = { Text("Email:", color = Color.White) },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                modifier = Modifier
                    .background(Color.Black)
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                textStyle = TextStyle(color = Color.White),
                label = { Text("Senha:", color = Color.White) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                modifier = Modifier
                    .background(Color.Black)
                    .padding(bottom = 3.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom=3.dp),
                horizontalArrangement = Arrangement.End

            ) {
                TextButton(onClick = {
                    val intent = Intent(context, ForgotPasswordActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Text("Recuperar senha", color = Color(0xFFB0BEC5))
                }
            }

            Button(
                onClick = {
                    val emailTrimmed = email.trim()
                    val senhaTrimmed = senha.trim()

                    Firebase.auth.signInWithEmailAndPassword(emailTrimmed, senhaTrimmed)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Login ok! Vai pra próxima tela
                                context.startActivity(Intent(context, homeActivity::class.java))
                                if (context is Activity) context.finish()
                            } else {
                                Toast.makeText(context, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                                Log.e("LOGIN", "Erro: ${task.exception?.message}")
                            }
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = verde),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 16.dp)
            ) {
                Text(text = "Login")
            }

            Button(
                onClick = {
                    val intent = Intent(context, registerActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = verde
                ),
                border = BorderStroke(0.8.dp, verde),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Não tenho conta", color = Color(0xFFE6EEE7))
            }
        }

        // Exibe o pop-up em cima de tudo assim que a tela carrega
        if (mostrarPopup) {
            MostrarPopupTermos(
                mostrarPopup = mostrarPopup,
                aoFechar = { mostrarPopup = false },
                aoAceitar = { mostrarPopup = false }
            )
        }
    }

}


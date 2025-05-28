package com.example.superid2

import android.R.attr.content
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.R
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            SuperID2Theme {
                PasswordApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordApp() {
    PasswordWithButton(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .wrapContentSize(Alignment.Center)
    )
}


@Composable
fun PasswordWithButton(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Botão de voltar no canto superior esquerdo
        IconButton(
            onClick = {
                if (context is Activity) {
                    context.finish()
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Formulário (Campos de entrada e botão)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.White) },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Button(
                onClick = {
                    val auth = FirebaseAuth.getInstance()
                    val trimmedEmail = email.trim() //remove espaco branco email

                    if (trimmedEmail.isNotEmpty()) {

                        auth.fetchSignInMethodsForEmail(trimmedEmail)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val methods = task.result?.signInMethods
                                    if (!methods.isNullOrEmpty() && methods.contains("password")) {
                                        // O email existe e esta associado ao login com senha
                                        auth.sendPasswordResetEmail(trimmedEmail) //envia redefinicao de senha
                                            .addOnCompleteListener { resetTask ->
                                                if (resetTask.isSuccessful) { //email enviado com sucesso
                                                    Toast.makeText(context, "E-mail de redefinição enviado.", Toast.LENGTH_SHORT).show()
                                                    if (context is Activity) context.finish()//fecha a tela atual
                                                } else {
                                                    Toast.makeText(context, "Erro ao enviar e-mail.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        //email existe, mas nao esta vinculado ao login
                                        Toast.makeText(context, "Este e-mail não tem login com senha.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    //N verifica metodos de login
                                    Toast.makeText(context, "Erro ao verificar e-mail.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        //campo de email invalido
                        Toast.makeText(context, "Digite um e-mail válido.", Toast.LENGTH_SHORT).show()
                    }


                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
            ) {
                Text("Redefinir Senha", color = Color.White)
            }
        }
    }
}
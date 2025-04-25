package com.example.superid2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.superid.R
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer

import androidx.compose.material.icons.filled.ArrowBack



class registerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperID2Theme {
                RegisterScreen(onRegisterSuccess = {

                })
            }
        }
    }
}
fun addNewUser(nome:String, email: String,senha: String) {
    val db = Firebase.firestore
    val inserir = hashMapOf(
        "Nome" to nome,
        "email" to email,
        "senha" to senha,
    )
    db.collection("Login").add(inserir)
}
// Tela de registrar (SignUp)
@SuppressLint("HardwareIds")
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        // Setinha de voltar
        val context = LocalContext.current

        Spacer(modifier = Modifier.padding(top = 20.dp)) // descendo a seta um pouco

        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Voltar",
            tint = Color.White,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(32.dp)
                .clickable {
                    val intent = Intent(context, loginActivity::class.java)
                    context.startActivity(intent)
                    if (context is Activity) {
                        context.finish() // fecha a tela de cadastro
                    }
                }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
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
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome", color = Color.White) },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
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

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha", color = Color.White) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                label = { Text("Confirme a senha", color = Color.White) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    // Confirmação se as senhas sao iguais
                    if (senha != confirmarSenha) {
                        Toast.makeText(context, "As senhas não coincidem.", Toast.LENGTH_SHORT)
                            .show()
                    } else if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                        Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        // Usando para criar uma conta no Auth (aqui tem requisito
                        // de email e senha pelo menos 6 digitos)
                        val auth = FirebaseAuth.getInstance()
                        auth.createUserWithEmailAndPassword(email, senha)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser?.uid
                                    val androidId = Settings.Secure.getString(
                                        context.contentResolver,
                                        Settings.Secure.ANDROID_ID
                                    )

                                    // Salva o Nome, email, uid e imei no firestore
                                    val userData = hashMapOf(
                                        "nome" to nome,
                                        "email" to email,
                                        "uid" to uid,
                                        "imei" to androidId
                                    )

                                    val db = Firebase.firestore
                                    if (uid != null) {
                                        db.collection("usuarios").document(uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Conta criada com sucesso!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onRegisterSuccess()
                                                val intent =
                                                    Intent(context, homeActivity::class.java)
                                                context.startActivity(intent)
                                                if (context is Activity) {
                                                    context.finish()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    context,
                                                    "Erro ao salvar dados: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Erro ao criar conta: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }
        }
    }
}

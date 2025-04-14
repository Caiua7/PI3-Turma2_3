package com.example.superid2

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
import com.google.firebase.firestore.firestore

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
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),
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
            leadingIcon = {
                Icon(Icons.Rounded.Person, contentDescription = null)
            },
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
        val context = LocalContext.current

        Button(
            onClick = {
                var mensagemErro = "" // Definição da variável para erro

                // Verificação de senhas
                if (senha != confirmarSenha) {
                    mensagemErro = "As senhas não coincidem."
                    Toast.makeText(context, mensagemErro, Toast.LENGTH_SHORT).show()
                } else if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                    // Verificação se todos os campos foram preenchidos
                    mensagemErro = "Preencha todos os campos."
                    Toast.makeText(context, mensagemErro, Toast.LENGTH_SHORT).show()
                } else {
                    // Adiciona usuário no Firestore
                    addNewUser(nome, email, senha) // Suponho que essa função já esteja implementada

                    // Chama função que trata o sucesso do registro
                    onRegisterSuccess()

                    // Redireciona para a próxima tela após sucesso
                    val intent = Intent(context, homeActivity::class.java)
                    context.startActivity(intent)

                    // Finaliza a tela atual
                    if (context is Activity) {
                        context.finish()
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



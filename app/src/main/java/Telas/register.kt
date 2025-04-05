package Telas

import com.example.superid.MainActivity
import com.example.superid.R


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperID2Theme {
                RegisterScreen(onRegisterSuccess = {
                    //abre a tela de login e finaliza essa tela
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
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
                Icon(Icons.Rounded.Person, contentDescription = null) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.White) },
            leadingIcon = {Icon(Icons.Rounded.Email, contentDescription = null)},
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
                //senhas diferentes
                if (senha != confirmarSenha) {
                    mensagemErro = "As senhas não coincidem."
                    //se algum campo estiver vazio
                } else if (nome.isEmpty() || email .isEmpty() || senha.isEmpty()) {
                    mensagemErro = "Preencha todos os campos."
                } else {
                    //add no firestore(banco de dados) e passa para proxima ela
                    addNewUser(nome,email,senha)
                    onRegisterSuccess()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }
        //Mensagem aparece em vermelho caso algo de errado
        if (mensagemErro.isNotEmpty()) {
            Text(text = mensagemErro, color = Color.Red)
        }
    }
}

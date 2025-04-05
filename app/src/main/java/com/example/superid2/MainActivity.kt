package com.example.superid

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

class MainActivity : ComponentActivity() {
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

/*fun createNewAccount(email:String, password:String) {
    val auth = Firebase.auth
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.i("AUTH_--TESTE", "ID do usuario ${user!!.uid}")
            } else {
                Log.i("AUTH-TESTE", "Usuario não criado")
            }
        }
}*/


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
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagemVisivel by remember { mutableStateOf(false) }


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Task Icon",
            modifier = Modifier
                .size(350.dp)
                .padding(bottom = 10.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            textStyle = TextStyle(color=Color.White),
            label = { Text(text = "Email:", color = Color.White) },
            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
            modifier = Modifier
                .background(Color.Black)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            textStyle = TextStyle(color=Color.White),
            label = { Text(text = "Senha: ", color = Color.White) },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = null) },
            modifier = Modifier
                .background(Color.Black)
                .padding(bottom = 32.dp)

        )

        Button(
            onClick = {
                mensagemVisivel = true

            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 16.dp)
        ) {
            Text(text = "Login")
        }

        if (mensagemVisivel) {
            Text(
                text = "Usuário adicionado com sucesso!",
                color = Color.Green,
                modifier = Modifier.padding(top = 8.dp)

            )
        }


    }
}

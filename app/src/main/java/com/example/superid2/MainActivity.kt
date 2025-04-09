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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.superid.Telas.TelaSenhasActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext

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

fun createNewAccount(context: Context, email: String, password: String) {
    val auth = Firebase.auth

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.i("AUTH", "Conta criada com sucesso: ${user?.uid}")
                Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()

                // Redireciona direto pra tela principal
                val intent = Intent(context, TelaSenhasActivity::class.java)
                context.startActivity(intent)
                if (context is Activity) context.finish()

            } else {
                Log.e("AUTH", "Erro ao criar conta", task.exception)
                Toast.makeText(context, "Erro ao criar conta: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
}


@Composable
fun LoginWithButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    val verde = Color(0xFF1B5E20)

    Column(
        modifier = modifier,
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
                .padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                val db = Firebase.firestore

                db.collection("Login")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val userDoc = documents.documents[0]
                            val senhaNoBanco = userDoc.getString("senha")

                            if (senhaNoBanco == senha) {
                                // Login bem-sucedido
                                context.startActivity(Intent(context, TelaSenhasActivity::class.java))
                                if (context is Activity) context.finish()
                            } else {
                                Toast.makeText(context, "Senha incorreta", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Email não encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Erro ao buscar usuário", Toast.LENGTH_SHORT).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = verde),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 16.dp)
        ) {
            Text(text = "Login")
        }
    }
}

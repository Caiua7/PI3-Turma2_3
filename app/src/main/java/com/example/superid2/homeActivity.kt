package com.example.superid2

import android.R.style
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import android.util.Base64
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import kotlin.io.encoding.ExperimentalEncodingApi

class homeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperID2Theme {
                TelaSenhas()
            }
        }
    }
}
//funcao para mandar os dados da senha para o firestore:
data class Senha(
    val titulo: String,
    val login: String,
    val senha: String,
    val accessToken: String
)

@Composable
fun TelaSenhas() {
    val verde = Color(0xFF4CAF50)
    var titulo by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val listaSenhas = remember { mutableStateListOf<Senha>() }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Cadastrar Nova Senha", color = verde, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (titulo.isNotBlank() && login.isNotBlank() && senha.isNotBlank()) {
                    val novoToken = gerarAccessToken() //gera token aleatorio
                    val novaSenha = Senha(titulo, login, senha, novoToken)
                    listaSenhas.add(novaSenha)

                    val db = FirebaseFirestore.getInstance()
                    val uid = FirebaseAuth.getInstance().currentUser?.uid //pega uid do usuario

                    if (uid != null) {
                        db.collection("usuarios") //acessa colecao usuarios
                            .document(uid) //pega uid do usuario
                            .collection("senhas") //adiciona o documento com os dados de senha
                            .add(hashMapOf(
                                "titulo" to novaSenha.titulo,
                                "login" to novaSenha.login,
                                "senha" to novaSenha.senha,
                                "accessToken" to novaSenha.accessToken
                            ))
                            .addOnSuccessListener {
                                Toast.makeText(context, "Senha adicionada!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Erro!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    //limpa os campos
                    titulo = ""
                    login = ""
                    senha = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = verde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Senhas Salvas",color = verde, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))
        //loop para mostrar as senhas
        for ((tituloSalvo, loginSalvo, senhaSalva) in listaSenhas) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🔐 $tituloSalvo", color = verde)
                    Text("Login: $loginSalvo", color = Color.White)
                    Text("Senha: $senhaSalva", color = Color.White)
                }
            }
        }
    }
}
//funcao para gerar token
@OptIn(ExperimentalEncodingApi::class)
fun gerarAccessToken(): String {
    val randomBytes = ByteArray(192) //cria um array de 192 bytes
    SecureRandom().nextBytes(randomBytes) //valores aleatorios
    val tokenBase64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP) //converte os bytes em string
    return tokenBase64.take(256) //garante que tenha 256 caracteres
}
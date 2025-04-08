package Telas

import android.R.style
import com.example.superid.MainActivity
import com.example.superid.R


import android.content.Intent
import android.media.tv.AdRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.core.os.postDelayed
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperID2Theme {
                SplashScreen()
            }
        }

        // Redireciona após 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }, 3000)
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
        var mostrarPopupTermos by remember { mutableStateOf(true) }
        var termosAceitos by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (mostrarPopupTermos) {
                TermosPopup(
                    onDismiss = { /* opcionalmente, não faz nada */ },
                    onAceitar = {
                        termosAceitos = true
                        mostrarPopupTermos = false
                    }
                )
            }
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
            Button(
                onClick = {
                    //aceitar termos
                    if (!termosAceitos) {
                        mensagemErro = "Você precisa aceitar os termos de uso."
                        //senhas diferentes
                    }else if (senha != confirmarSenha) {
                        mensagemErro = "As senhas não coincidem."
                        //se algum campo estiver vazio
                    } else if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                        mensagemErro = "Preencha todos os campos."
                    } else {
                        //add no firestore(banco de dados) e passa para proxima ela
                        addNewUser(nome, email, senha)
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

@Composable
fun TermosPopup(
    onDismiss: () -> Unit,
    onAceitar: () -> Unit
) {
    //texto para colocar termos
    val termos = """
        Bem Vindo ao SuperID, leia atentamente os termos:
        1 - aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.
        2 - bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.
        3 - cccccccccccccccccccccccccccccccccccccccccccccccccccccccc.
        
        Clique em "Aceitar" para começar o cadastro!
    """.trimIndent()

    var checkAceito by remember { mutableStateOf(false) }
    //Criar pop-up
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                val verde = Color(0xFF4CAF50)
                //titulo
                Text(
                    "Termos de Uso",
                    style = MaterialTheme.typography.headlineSmall,
                    color = verde

                )

                Spacer(modifier = Modifier.height(10.dp))
                //Area com termos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()) //deixa rolavel
                        .padding(8.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))

                ) {
                    Text(
                        text = termos,
                        color = verde,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row da checkbox com o texto
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = checkAceito,// false
                        onCheckedChange = { checkAceito = it },//aqui vira true
                        colors = CheckboxDefaults.colors(
                            checkedColor = verde,
                            uncheckedColor = verde
                        )
                    )
                    Text("Li e aceito os termos", color = verde)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Linha com botao
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (checkAceito) {
                                onAceitar()
                                onDismiss()
                            }
                        },
                        enabled = checkAceito, //Habilita botao qnd checkbox
                        colors = ButtonDefaults.buttonColors(containerColor = verde)
                    ) {
                        Text("Aceitar", color = Color.Black)
                    }
                }
            }
        }
    }
}
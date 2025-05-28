package com.example.superid2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.superid.R

import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlin.jvm.java
import androidx.core.content.edit

//Tela de descricao do APP

/*
// DEIXAR AQUI PARA TESTES, NAO PRECISA DESINSTALAR E INSTALAR TODA VEZ PARA TESTAR DESCRIÇAO.
class descricaoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperID2Theme {
                val mostrarDescricao by remember { mutableStateOf(true) }

                if (mostrarDescricao) {
                    ExibirCardDescricao(
                        mostrarDescricao = true,
                        aoFechar = {
                            //Problema da tela branca abaixo
                            //mostrarDescricao = false
                            startActivity(Intent(this@descricaoActivity, loginActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}
*/

class descricaoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verifica se o app foi aberto pela primeira vez
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        // Se for a primeira vez, exibe a tela de descrição
        if (isFirstLaunch) {
            setContent {
                SuperID2Theme {
                    val mostrarDescricao by remember { mutableStateOf(true) }

                    if (mostrarDescricao) {
                        ExibirCardDescricao(
                            mostrarDescricao = true,
                            aoFechar = {
                                // Salva no SharedPreferences que o app já foi aberto
                                sharedPreferences.edit() { putBoolean("isFirstLaunch", false) }

                                // Redireciona para a tela de login
                                startActivity(Intent(this@descricaoActivity, loginActivity::class.java))
                                finish()
                            }
                        )
                    }
                }
            }
        } else {
            // Caso já tenha sido aberto antes, redireciona direto para a tela de login
            startActivity(Intent(this@descricaoActivity, loginActivity::class.java))
            finish()
        }
    }
}

@Composable
fun ExibirCardDescricao(
    mostrarDescricao: Boolean,
    aoFechar: () -> Unit
) {
    if (mostrarDescricao) {
        val verde = Color(0xFF4CAF50)

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "O SuperID é um app de autenticação e gerenciamento de senhas, " +
                                "criado para te ajudar a manter seus logins organizados e com mais praticidade.",
                        color = verde,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = aoFechar,
                        colors = ButtonDefaults.buttonColors(containerColor = verde)
                    ) {
                        Text("Continuar", color = Color.Black)
                    }
                }
            }
        }
    }
}


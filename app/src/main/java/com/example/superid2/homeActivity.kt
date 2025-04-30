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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import kotlin.io.encoding.ExperimentalEncodingApi

// usuário gerencia suas senhas
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

data class Senha(
    val titulo: String,
    val login: String,
    val senha: String,
    val accessToken: String,
    val categoria: String
)

data class SenhaCriptografada(
    val senha: String,
    val iv: String
)


@Composable
fun TelaSenhas() {
    val verde = Color(0xFF4CAF50)
    var titulo by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val listaSenhas = remember { mutableStateListOf<Senha>() }
    val context = LocalContext.current

    var selectedCategoria by remember { mutableStateOf("Sites Web") }
    val categorias = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico")
    var filtroCategoria by remember { mutableStateOf("Todas") }
    var mostrarOpcoesCategoria by remember { mutableStateOf(false) }

    val cryptoManager = CryptoManager()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid // UID do usuário que logou

    // Quando a tela for carregada, buscar as senhas salvas
    LaunchedEffect(Unit) {  // executa uma vez quando composable abre
        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .collection("senhas")
                .get() // le todos os documentos dentro de (usuarios/uid/senhas)
                .addOnSuccessListener { result ->
                    listaSenhas.clear() // Limpa a lista local
                    for (document in result) {
                        val titulo = document.getString("titulo") ?: ""
                        val login = document.getString("login") ?: ""
                        val senha = document.getString("senha") ?: ""
                        val iv = document.getString("iv") ?: ""
                        val senhaDescriptografada = try {
                            val ivBytes = Base64.decode(iv, Base64.NO_WRAP)
                            val encryptedBytes = Base64.decode(senha, Base64.NO_WRAP)
                            val descriptografia = cryptoManager.decrypt(ivBytes, encryptedBytes)
                            descriptografia.toString(Charsets.UTF_8)
                        } catch (e: Exception) {
                            "Erro ao descriptografar"
                        }
                        val accessToken = document.getString("accessToken") ?: ""
                        val categoria = document.getString("categoria") ?: "Sites Web" // padrão
                        listaSenhas.add(Senha(titulo, login, senhaDescriptografada, accessToken, categoria))

                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao carregar senhas", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Layout da tela
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título da seção de cadastro
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

        // Botão "Categoria"
        OutlinedTextField(
            value = selectedCategoria,
            onValueChange = {}, // Não permite digitar
            label = { Text("Categoria", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { mostrarOpcoesCategoria = !mostrarOpcoesCategoria }, // Abre o menu ao clicar
            enabled = false, // desativa digitação
            readOnly = true // somente leitura
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Se mostrarOpcoesCategoria for true, mostra as opções
        if (mostrarOpcoesCategoria) {
            categorias.forEach { categoria ->
                Button(
                    onClick = {
                        selectedCategoria = categoria
                        mostrarOpcoesCategoria = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text(categoria, color = Color.White)
                }
            }
        }

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

        // Botão de salvar senha
        Button(
            onClick = {
                if (titulo.isNotBlank() && login.isNotBlank() && senha.isNotBlank()) {
                    val bytes = senha.encodeToByteArray()
                    val (iv, senhaCriptografada) = cryptoManager.encrypt(bytes)
                    val senhaCriptografadaString = Base64.encodeToString(senhaCriptografada, Base64.NO_WRAP)
                    val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
                    val novoToken = gerarAccessToken() // Gerar token aleatório
                    val novaSenha = Senha(titulo, login, senha, novoToken, selectedCategoria)
                    val novaSenhaCriptografada = SenhaCriptografada(senhaCriptografadaString, ivString)
                    listaSenhas.add(novaSenha) // Adicionar na lista local

                    if (uid != null) {
                        db.collection("usuarios")
                            .document(uid)
                            .collection("senhas")
                            .add(hashMapOf(
                                "titulo" to novaSenha.titulo,
                                "login" to novaSenha.login,
                                "senha" to novaSenhaCriptografada.senha,
                                "iv" to novaSenhaCriptografada.iv,
                                "accessToken" to novaSenha.accessToken,
                                "categoria" to novaSenha.categoria // <-- aqui
                            ))
                            //add todas as infos dentro de senha pro usuario
                            .addOnSuccessListener {
                                Toast.makeText(context, "Senha adicionada!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Erro!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    // Limpar os campos depois de salvar
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

        // Título da seção de senhas salvas
        Text("Senhas Salvas", color = verde, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        // --------------------------------------------
        // FILTRO DA CATEGORIA
        Spacer(modifier = Modifier.height(8.dp))

        Text("Filtrar por Categoria:", color = Color.White, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        val opcoesFiltro = listOf("Todas", "Sites Web", "Aplicativos", "Teclados de Acesso Físico")
        opcoesFiltro.forEach { opcao ->
            Button(
                onClick = { filtroCategoria = opcao },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (filtroCategoria == opcao) verde else Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                Text(opcao, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //----------------------------------------------

        // Listar todas as senhas já carregadas
        listaSenhas
            .filter { filtroCategoria == "Todas" || it.categoria == filtroCategoria }
            .forEach { senhaItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🔐 ${senhaItem.titulo}", color = verde)
                        Text("Categoria: ${senhaItem.categoria}", color = Color.Gray)
                        Text("Login: ${senhaItem.login}", color = Color.White)
                        Text("Senha: ${senhaItem.senha}", color = Color.White)
                    }
                }
            }
    }
}

// Função para gerar um token de acesso seguro
@OptIn(ExperimentalEncodingApi::class)
fun gerarAccessToken(): String {
    val randomBytes = ByteArray(192) // Cria um array de 192 bytes
    SecureRandom().nextBytes(randomBytes) // Preenche com valores aleatórios
    val tokenBase64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP) // Codifica em base64
    return tokenBase64.take(256) // Retorna 256 caracteres
}
package com.example.superid2
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.res.painterResource
import com.example.superid.R

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width





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

data class Senha( //armazenar senha indvidualmente
    val titulo: String,
    val login: String,
    val senha: String,
    val accessToken: String,
    val categoria: String
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
    var showDialog by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    //funcao que executa para pegar as senhas ja realizadas antes pelo usuario
    LaunchedEffect(Unit) {
        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .collection("senhas")
                .get()
                .addOnSuccessListener { result ->
                    listaSenhas.clear()
                    for (document in result) {
                        val titulo = document.getString("titulo") ?: ""
                        val login = document.getString("login") ?: ""
                        val senha = document.getString("senha") ?: ""
                        val accessToken = document.getString("accessToken") ?: ""
                        val categoria = document.getString("categoria") ?: "Sites Web"
                        listaSenhas.add(Senha(titulo, login, senha, accessToken, categoria))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao carregar senhas", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Senhas Salvas", color = verde, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))

            var mostrarFiltro by remember { mutableStateOf(false) }

            //botao para mostar o filtro
            TextButton(onClick = { mostrarFiltro = !mostrarFiltro }) {
                Text("Filtrar por categoria", color = Color.White)
            }

            if (mostrarFiltro) {

                val opcoesFiltro =
                    listOf("Todas", "Sites Web", "Aplicativos", "Teclados de Acesso Físico")
                opcoesFiltro.forEach { opcao ->
                    Button(
                        onClick = { filtroCategoria = opcao },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (filtroCategoria == opcao) verde else Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(opcao, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            //exibicao das senhas filtradas


            // variáveis de estado para edição/exclusão
            var senhaParaEditar by remember { mutableStateOf<Senha?>(null) }
            var senhaParaExcluir by remember { mutableStateOf<Senha?>(null) }

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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { senhaParaEditar = senhaItem }) {
                                    Text("EDITAR", color = verde)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { senhaParaExcluir = senhaItem }) {
                                    Text("EXCLUIR", color = Color.Red)
                                }
                            }
                        }
                    }
                }

            // Filtrei pelo AcessToken para nao ter erro com titulo igual
            // Popup de edição
            senhaParaEditar?.let { senha ->
                var novoTitulo by remember { mutableStateOf(senha.titulo) }
                var novoLogin by remember { mutableStateOf(senha.login) }
                var novaSenha by remember { mutableStateOf(senha.senha) }
                var novaCategoria by remember { mutableStateOf(senha.categoria) }
                var mostrarOpcoesCategoria by remember { mutableStateOf(false) }

                val categorias = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico")

                AlertDialog(
                    onDismissRequest = { senhaParaEditar = null },
                    title = { Text("Editar Senha", color = verde) },
                    containerColor = Color.Black,
                    text = {
                        Column {
                            OutlinedTextField(
                                value = novoTitulo,
                                onValueChange = { novoTitulo = it },
                                label = { Text("Título", color = Color.White) },
                                textStyle = TextStyle(color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // PERGUNTAR PRO MATEUS COMO COLOCAR BORDA NESSE BOTAO. ERRO DE IMPORTAÇAO
                            OutlinedTextField(
                                value = novaCategoria,
                                onValueChange = {},
                                label = { Text("Categoria", color = Color.White) },
                                textStyle = TextStyle(color = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mostrarOpcoesCategoria = !mostrarOpcoesCategoria },
                                enabled = false,
                                readOnly = true
                            )

                            if (mostrarOpcoesCategoria) {
                                categorias.forEach { categoria ->
                                    Button(
                                        onClick = {
                                            novaCategoria = categoria
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

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = novoLogin,
                                onValueChange = { novoLogin = it },
                                label = { Text("Login", color = Color.White) },
                                textStyle = TextStyle(color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = novaSenha,
                                onValueChange = { novaSenha = it },
                                label = { Text("Senha", color = Color.White) },
                                textStyle = TextStyle(color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val docRef = db.collection("usuarios").document(uid!!).collection("senhas")
                                docRef.whereEqualTo("accessToken", senha.accessToken).get()
                                    .addOnSuccessListener { query ->
                                        for (document in query) {
                                            document.reference.update(
                                                mapOf(
                                                    "titulo" to novoTitulo,
                                                    "login" to novoLogin,
                                                    "senha" to novaSenha,
                                                    "categoria" to novaCategoria
                                                )
                                            )
                                        }
                                        listaSenhas.remove(senha)
                                        listaSenhas.add(
                                            senha.copy(
                                                titulo = novoTitulo,
                                                login = novoLogin,
                                                senha = novaSenha,
                                                categoria = novaCategoria
                                            )
                                        )
                                        senhaParaEditar = null
                                    }
                            }
                        ) {
                            Text("SALVAR", color = verde)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { senhaParaEditar = null }) {
                            Text("Cancelar", color = Color.Red)
                        }
                    }
                )
            }

            // Filtrei pelo AcessToken para nao ter erro com titulo igual
            // Popup de exclusão
            senhaParaExcluir?.let { senha ->
                AlertDialog(
                    onDismissRequest = { senhaParaExcluir = null },
                    title = { Text("Confirmação", color = verde) },
                    containerColor = Color.Black,
                    text = { Text("TEM CERTEZA QUE DESEJA EXCLUIR ESSE LOGIN?", color = Color.White) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val docRef = db.collection("usuarios").document(uid!!).collection("senhas")
                                docRef.whereEqualTo("accessToken", senha.accessToken).get()
                                    .addOnSuccessListener { query ->
                                        for (document in query) {
                                            document.reference.delete()
                                        }
                                        listaSenhas.remove(senha)
                                        senhaParaExcluir = null
                                    }
                            }
                        ) {
                            Text("SIM", color = verde)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { senhaParaExcluir = null }) {
                            Text("CANCELAR", color = Color.Red)
                        }
                    }
                )
            }


        }
        //botao para chamar pop up de adicionar senha
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = verde,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Adicionar nova senha",
                tint = Color.White
            )
        }
        //exibir pop up
        if (showDialog) {
            AlertDialog(
                containerColor = Color.Black,
                onDismissRequest = {},
                confirmButton = {},
                dismissButton = { //fechar o pop up
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar",color = Color.Red)
                    }
                },
                title = {
                    Text("Cadastrar Nova Senha", color = verde, style = MaterialTheme.typography.headlineSmall)
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Título", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        //exibir opcoes de categoria
                        OutlinedTextField(
                            value = selectedCategoria,
                            onValueChange = {},
                            label = { Text("Categoria", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarOpcoesCategoria = !mostrarOpcoesCategoria },
                            enabled = false,
                            readOnly = true
                        )

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
                        //salva senha e manda pro bd
                        Button(
                            onClick = {
                                if (titulo.isNotBlank() && login.isNotBlank() && senha.isNotBlank()) {
                                    val novoToken = gerarAccessToken()
                                    val novaSenha = Senha(titulo, login, senha, novoToken, selectedCategoria)
                                    listaSenhas.add(novaSenha)

                                    if (uid != null) {
                                        db.collection("usuarios")
                                            .document(uid)
                                            .collection("senhas")
                                            .add(
                                                hashMapOf(
                                                    "titulo" to novaSenha.titulo,
                                                    "login" to novaSenha.login,
                                                    "senha" to novaSenha.senha,
                                                    "accessToken" to novaSenha.accessToken,
                                                    "categoria" to novaSenha.categoria
                                                )
                                            )
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Senha adicionada!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Erro!", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    //Limpa campos!
                                    titulo = ""
                                    login = ""
                                    senha = ""
                                    showDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = verde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Salvar", color = Color.Black)
                        }
                    }
                }
            )
        }
    }
}
//gerar token aleatorio de acesso
@OptIn(ExperimentalEncodingApi::class)
fun gerarAccessToken(): String {
    val randomBytes = ByteArray(192)
    SecureRandom().nextBytes(randomBytes)
    val tokenBase64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
    return tokenBase64.take(256)
}


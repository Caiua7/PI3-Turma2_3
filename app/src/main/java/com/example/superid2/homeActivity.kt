package com.example.superid2

// ------------------------------- IMPORTS ----------------------------------

import androidx.compose.material3.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.superid2.ui.theme.SuperID2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.superid.R
import kotlin.text.encodeToByteArray

// --------------------------- ACTIVITY PRINCIPAL ----------------------------

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

// -------------------------- DATA CLASSES -----------------------------------

data class Senha(
    val titulo: String,
    val login: String,
    val senha: String,
    val accessToken: String,
    val categoria: String,
    val descricao: String = ""
)

data class SenhaCriptografada(
    val senha: String,
    val iv: String
)

// -------------------------- TELA PRINCIPAL ---------------------------------

@Composable
fun TelaSenhas() {
    // ----------- VARIÁVEIS DE ESTADO -----------
    val verde = Color(0xFF4CAF50)
    var titulo by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val listaSenhas = remember { mutableStateListOf<Senha>() }
    val context = LocalContext.current
    var selectedCategoria by remember { mutableStateOf("Sites Web") }
    val categorias = remember { mutableStateListOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") }
    var filtroCategoria by remember { mutableStateOf("Todas") }
    var mostrarOpcoesCategoria by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    var showAdicionarCategoria by remember { mutableStateOf(false) }
    var novaCategoriaTexto by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var mostrarExcluirCategoria by remember { mutableStateOf(false) }
    val cryptoManager = CryptoManager()
    val categoriasFixas = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico")

    // ------------------ Carregamento Inicial ------------------
    LaunchedEffect(Unit) {
        if (uid != null) {
            // Carregar senhas
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
                        val categoria = document.getString("categoria") ?: "Sites Web"
                        listaSenhas.add(Senha(titulo, login, senhaDescriptografada, accessToken, categoria))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao carregar senhas", Toast.LENGTH_SHORT).show()
                }

            // Carregar categorias
            db.collection("usuarios")
                .document(uid)
                .collection("categorias")
                .get()
                .addOnSuccessListener { result ->
                    val categoriasFirebase = mutableSetOf<String>()
                    for (document in result) {
                        val nome = document.getString("nome")
                        if (!nome.isNullOrEmpty()) {
                            categoriasFirebase.add(nome)
                        }
                    }
                    val todasCategorias = (categoriasFixas + categoriasFirebase).toSet()
                    categorias.clear()
                    categorias.addAll(todasCategorias)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao carregar categorias", Toast.LENGTH_SHORT).show()
                    categorias.clear()
                    categorias.addAll(categoriasFixas)
                }
        }
    }

    // -------------------------- UI PRINCIPAL -------------------------------

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ----------------- TÍTULO BONITO DA PÁGINA ------------------
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Ícone de Senha",
                        tint = verde,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Senhas Salvas",
                        color = verde,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Divider(
                    color = verde.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(end = 64.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // ------------------- BOTÕES DE AÇÃO EM LINHA --------------------
            var mostrarFiltro by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val buttonShape = RoundedCornerShape(12.dp)
                TextButton(
                    onClick = { mostrarFiltro = !mostrarFiltro },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                        .height(36.dp)
                        .clip(buttonShape)
                        .background(Color(0xFF2A2A2A)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Filtrar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtrar", color = Color.White, fontSize = 12.sp)
                }
                TextButton(
                    onClick = { mostrarExcluirCategoria = !mostrarExcluirCategoria },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                        .height(36.dp)
                        .clip(buttonShape)
                        .background(Color(0xFF2A2A2A)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remover Categoria",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Categoria", color = Color.White, fontSize = 12.sp)
                }
                TextButton(
                    onClick = { showAdicionarCategoria = true },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                        .height(36.dp)
                        .clip(buttonShape)
                        .background(Color(0xFF2A2A2A)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar Categoria",
                        tint = verde,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Categoria", color = verde, fontSize = 12.sp)
                }
            }

            // ------------------- DIALOGS DE CATEGORIA ---------------------
            if (showAdicionarCategoria) {
                AlertDialog(
                    onDismissRequest = { showAdicionarCategoria = false },
                    title = { Text("Nova Categoria", color = verde) },
                    containerColor = Color.Black,
                    text = {
                        OutlinedTextField(
                            value = novaCategoriaTexto,
                            onValueChange = { novaCategoriaTexto = it },
                            label = { Text("Nome da Categoria", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val novaCategoria = novaCategoriaTexto.trim()
                                if (novaCategoria.isNotEmpty() && novaCategoria !in categorias && uid != null) {
                                    categorias.add(novaCategoria)
                                    val categoriaData = hashMapOf("nome" to novaCategoria)
                                    db.collection("usuarios")
                                        .document(uid)
                                        .collection("categorias")
                                        .add(categoriaData)
                                        .addOnSuccessListener {
                                            println("Categoria salva no Firebase.")
                                        }
                                        .addOnFailureListener { e ->
                                            println("Erro ao salvar categoria: ${e.message}")
                                        }
                                }
                                novaCategoriaTexto = ""
                                showAdicionarCategoria = false
                            }
                        ) {
                            Text("Adicionar", color = verde)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                novaCategoriaTexto = ""
                                showAdicionarCategoria = false
                            }
                        ) {
                            Text("Cancelar", color = Color.Red)
                        }
                    }
                )
            }

            if (mostrarExcluirCategoria) {
                val categoriasRemoviveis = categorias.filterNot { it in categoriasFixas }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.Black)
                ) {
                    if (categoriasRemoviveis.isEmpty()) {
                        Text(
                            "Nenhuma categoria personalizada",
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        categoriasRemoviveis.forEach { categoria ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            categorias.remove(categoria)
                                            db.collection("usuarios")
                                                .document(uid ?: "")
                                                .collection("categorias")
                                                .whereEqualTo("nome", categoria)
                                                .get()
                                                .addOnSuccessListener { result ->
                                                    for (document in result) {
                                                        db.collection("usuarios")
                                                            .document(uid ?: "")
                                                            .collection("categorias")
                                                            .document(document.id)
                                                            .delete()
                                                    }
                                                }
                                            mostrarExcluirCategoria = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = categoria,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remover categoria",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---------------------- FILTRO DE CATEGORIA ---------------------
            if (mostrarFiltro) {
                val opcoesFiltro = listOf("Todas") + categorias
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

            // -------------------- LISTAGEM DAS SENHAS ---------------------
            var senhaParaEditar by remember { mutableStateOf<Senha?>(null) }
            var senhaParaExcluir by remember { mutableStateOf<Senha?>(null) }

            listaSenhas
                .filter { filtroCategoria == "Todas" || it.categoria == filtroCategoria }
                .forEach { senhaItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🔐 ${senhaItem.titulo}",
                                color = verde,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Descricao: ${senhaItem.descricao}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Categoria: ${senhaItem.categoria}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Login: ${senhaItem.login}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Senha: ${senhaItem.senha}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { senhaParaEditar = senhaItem }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = verde,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("EDITAR", color = verde)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { senhaParaExcluir = senhaItem }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("EXCLUIR", color = Color.Red)
                                }
                            }
                        }
                    }
                }

            // ------------------ ALERT DE EDIÇÃO DE SENHA ---------------------
            senhaParaEditar?.let { senha ->
                var novoTitulo by remember { mutableStateOf(senha.titulo) }
                var novoLogin by remember { mutableStateOf(senha.login) }
                var novaSenha by remember { mutableStateOf(senha.senha) }
                var novaCategoria by remember { mutableStateOf(senha.categoria) }
                var mostrarOpcoesCategoria by remember { mutableStateOf(false) }
                val bytes = novaSenha.encodeToByteArray()
                val (iv, senhaCriptografada) = cryptoManager.encrypt(bytes)
                val novaSenhaCriptografada = Base64.encodeToString(senhaCriptografada, Base64.NO_WRAP)
                val novoIv = Base64.encodeToString(iv, Base64.NO_WRAP)
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
                                                    "iv" to novoIv,
                                                    "titulo" to novoTitulo,
                                                    "login" to novoLogin,
                                                    "senha" to novaSenhaCriptografada,
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

            // ----------------- ALERT DE EXCLUSÃO DE SENHA ---------------------
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

        // -------------- BOTÃO DE ADD SENHA (FAB) ---------------
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
        // -------------- BOTÃO QR CODE (FAB) -------------------
        FloatingActionButton(
            onClick = { /* sua ação */ },
            containerColor = verde,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.qrcode), // coloque o arquivo na pasta drawable
                contentDescription = "Gerar QR Code",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        // --------------- ALERT DE NOVA SENHA ------------------
        if (showDialog) {
            AlertDialog(
                containerColor = Color.Black,
                onDismissRequest = {},
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar", color = Color.Red)
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
                            label = { Text("Título (opcional)", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = descricao,
                            onValueChange = { descricao = it },
                            label = { Text("Descrição (opcional)", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                            label = { Text("Login (opcional)", color = Color.White) },
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
                                if (senha.isNotBlank() && selectedCategoria.isNotBlank()) {
                                    val bytes = senha.encodeToByteArray()
                                    val (iv, senhaCriptografada) = cryptoManager.encrypt(bytes)
                                    val senhaCriptografadaString = Base64.encodeToString(senhaCriptografada, Base64.NO_WRAP)
                                    val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
                                    val novoToken = gerarAccessToken()
                                    val novaSenha = Senha(titulo, login, senha, novoToken, selectedCategoria, descricao)
                                    val novaSenhaCriptografada = SenhaCriptografada(senhaCriptografadaString, ivString)
                                    listaSenhas.add(novaSenha)
                                    if (uid != null) {
                                        db.collection("usuarios")
                                            .document(uid)
                                            .collection("senhas")
                                            .add(
                                                hashMapOf(
                                                    "titulo" to titulo,
                                                    "descricao" to descricao,
                                                    "login" to login,
                                                    "senha" to novaSenhaCriptografada.senha,
                                                    "iv" to novaSenhaCriptografada.iv,
                                                    "accessToken" to novoToken,
                                                    "categoria" to selectedCategoria
                                                )
                                            )
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Senha adicionada!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Erro!", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    titulo = ""
                                    descricao = ""
                                    login = ""
                                    senha = ""
                                    selectedCategoria = ""
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

// ------------------- GERADOR DE ACCESS TOKEN -----------------------

@OptIn(ExperimentalEncodingApi::class)
fun gerarAccessToken(): String {
    val randomBytes = ByteArray(192)
    SecureRandom().nextBytes(randomBytes)
    val tokenBase64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
    return tokenBase64.take(256)
}


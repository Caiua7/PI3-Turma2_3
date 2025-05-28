package com.example.superid2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun TermosPopup(
    onDismiss: () -> Unit, //funcao quando quer fechar pop up
    onAceitar: () -> Unit // quando aceitar os termos
) {
    //texto para colocar termos
    val termos = """
    Bem-vindo ao SuperID! Leia atentamente os termos de uso antes de utilizar nosso aplicativo:

    1. **Sobre o Aplicativo**  
    O SuperID é um gerenciador de autenticações que permite o cadastro, armazenamento e uso de credenciais de forma segura. 
    O aplicativo foi desenvolvido para proporcionar uma maneira prática e segura de gerenciar suas senhas e facilitar o login em diferentes serviços.

    2. **Dados Coletados**  
    Ao utilizar o SuperID, você concorda em fornecer as seguintes informações:
    - **E-mail e UID** (identificador único) do Firebase Authentication;
    - **Senhas** e **logins** que você cadastrar dentro do aplicativo;
    - **Tokens de acesso** gerados para autenticação interna.

    Esses dados são armazenados em serviços do **Google Firebase**, de forma privada e associada exclusivamente à sua conta.

    3. **Uso dos Dados**  
    As informações coletadas têm como finalidade:
    - Gerenciar suas credenciais de forma segura no aplicativo;
    - Permitir seu login e identificação dentro do app;
    - Melhorar a experiência de uso no sistema.

    Nenhum dado será compartilhado com terceiros ou utilizado para fins comerciais.

    4. **Riscos e Limitações**  
    - O aplicativo **não garante** segurança absoluta dos dados.
    - Não nos responsabilizamos por eventuais perdas de dados ou acessos indevidos decorrentes de falhas técnicas.
    
    5. **Consentimento**  
    Ao clicar em "Aceitar", você declara que:
    - Leu e compreendeu estes Termos de Uso e a Política de Privacidade;
    - Concorda com a coleta, armazenamento e utilização dos seus dados conforme descrito;
       - Está ciente de que o aplicativo não oferece garantias plenas de segurança.

    

    **Clique em "Aceitar" para começar o cadastro!**
""".trimIndent()

    var checkAceito by remember { mutableStateOf(false) } //checkbox
    //Criar pop-up
    Dialog(onDismissRequest ={ /* ao clicar fora nao acontece nada */}) {
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
                            if (checkAceito)  { //se a checkbox estiver marcada
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
@Composable
fun MostrarPopupTermos(
    mostrarPopup: Boolean, //define se deve ser mostrado ou nao
    aoFechar: () -> Unit,
    aoAceitar: () -> Unit
) {
    if (mostrarPopup) { //true mostra o popup
        TermosPopup(
            onDismiss = aoFechar,
            onAceitar = { //quando usuario aceitar
                aoAceitar() //marca que aceitou
                aoFechar() //fecha
            }
        )
    }
}
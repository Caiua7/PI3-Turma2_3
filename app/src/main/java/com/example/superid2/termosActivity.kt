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
        Bem Vindo ao SuperID, leia atentamente os termos:
        1 - aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.
        2 - bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.
        3 - cccccccccccccccccccccccccccccccccccccccccccccccccccccccc.
        
        Clique em "Aceitar" para começar o cadastro!
    """.trimIndent()

    var checkAceito by remember { mutableStateOf(false) } //controla a checkbox
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
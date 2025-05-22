package com.example.superid2

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun RequestPermissionScreen(
    modifier: Modifier = Modifier,
    permission: String,
    permissionTextButton: String,
    onPermissionGranted: () -> Unit
){
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionGranted()
    }

    Box(modifier = Modifier.fillMaxSize()){
        Button(
            modifier = modifier.align(Alignment.Center),
            onClick = {launcher.launch(permission) }
        ) {
            Text(permissionTextButton)
        }
    }
}

@Composable
fun WithPermission(
    modifier: Modifier = Modifier,
    permission: String,
    permissionTextButton: String,
    content: @Composable () -> Unit
){
    // obtendo o contexto local do app
    val context = LocalContext.current

    var permissionGranted by remember {
        mutableStateOf(context.checkSelfPermission(permission) ==
                PackageManager.PERMISSION_GRANTED)
    }

    if(!permissionGranted){
        // pedir a permissão para o usuário
        RequestPermissionScreen(
            modifier = modifier,
            permission = permission,
            permissionTextButton = permissionTextButton
        ) {
            permissionGranted = true
        }
    }else{
        // seguir para a outra função composable
        Surface (modifier = modifier){
            content()
        }
    }
}

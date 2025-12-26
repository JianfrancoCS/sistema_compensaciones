package com.agropay

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.agropay.di.appModule
import com.agropay.ui.navigation.Navigation
import com.agropay.ui.theme.AgroPayTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinAppDeclaration

@Composable
@Preview
fun App(
    koinDeclaration: KoinAppDeclaration? = null
) {
    KoinApplication(
        application = {
            koinDeclaration?.invoke(this)
            modules(appModule)
        }
    ) {
        AgroPayTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Navigation()
            }
        }
    }
}

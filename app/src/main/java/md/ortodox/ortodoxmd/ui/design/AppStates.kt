package md.ortodox.ortodoxmd.ui.design

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppLoading(modifier: Modifier = Modifier) {
    Box(Modifier.fillMaxSize().then(modifier), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun AppEmpty(message: String, modifier: Modifier = Modifier) {
    Box(Modifier.fillMaxSize().then(modifier), contentAlignment = Alignment.Center) {
        Text(message)
    }
}

@Composable
fun AppError(message: String, modifier: Modifier = Modifier) {
    Box(Modifier.fillMaxSize().then(modifier), contentAlignment = Alignment.Center) {
        Text(message)
    }
}

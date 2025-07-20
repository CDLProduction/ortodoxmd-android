package md.ortodox.ortodoxmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import md.ortodox.ortodoxmd.features.calendar.ui.CalendarScreen  // Importă screen-ul calendar (creează-l ulterior dacă nu există)
import md.ortodox.ortodoxmd.ui.theme.OrtodoxMDTheme  // Theme custom; folosește MaterialTheme dacă nu ai creat

@AndroidEntryPoint  // Activează Hilt DI pentru activity (analog @SpringBootApplication pentru injection)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrtodoxMDTheme {  // Wrapper pentru theme Material3 (best practice pentru consistență UI)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()  // Controller pentru navigare (analog router în backend)
                    NavHost(navController = navController, startDestination = "calendar") {  // NavHost pentru rute (modular, ușor de extins)
                        composable("calendar") { CalendarScreen() }  // Rută inițială pentru calendar; adaugă altele iterativ (ex: "bible")
                        // Ex: composable("bible") { BibleScreen() }
                    }
                }
            }
        }
    }
}
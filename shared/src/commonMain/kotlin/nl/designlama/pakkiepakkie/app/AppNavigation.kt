package nl.designlama.pakkiepakkie.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nl.designlama.pakkiepakkie.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object Home

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize().then(modifier),
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<Home> {
                HomeScreen()
            }
        }
    }
}

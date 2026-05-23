package nl.designlama.pakkiepakkie.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import nl.designlama.pakkiepakkie.ui.HomeScreen
import nl.designlama.pakkiepakkie.ui.SettingsScreen
import nl.designlama.pakkiepakkie.ui.VehicleCompareScreen

@Serializable
data object HomeRoute

@Serializable
data class VehicleDetail(val kenteken: String)

@Serializable
data object SettingsRoute

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize().then(modifier),
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onOpenVehicleDetail = { k ->
                        navController.navigate(VehicleDetail(kenteken = k))
                    },
                    onOpenSettings = {
                        navController.navigate(SettingsRoute)
                    },
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable<VehicleDetail> { entry ->
                val route = entry.toRoute<VehicleDetail>()
                VehicleCompareScreen(
                    kenteken = route.kenteken,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

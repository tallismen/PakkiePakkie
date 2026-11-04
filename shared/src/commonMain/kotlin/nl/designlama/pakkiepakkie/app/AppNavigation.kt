package nl.designlama.pakkiepakkie.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import nl.designlama.pakkiepakkie.datastore.OnboardingRepository
import nl.designlama.pakkiepakkie.ui.HomeScreen
import nl.designlama.pakkiepakkie.ui.OnboardingScreen
import nl.designlama.pakkiepakkie.ui.SettingsScreen
import nl.designlama.pakkiepakkie.ui.TestMenuScreen
import nl.designlama.pakkiepakkie.ui.VehicleCompareScreen
import org.koin.compose.koinInject

@Serializable
data object OnboardingRoute

@Serializable
data object HomeRoute

@Serializable
data class VehicleDetail(val kenteken: String)

@Serializable
data object SettingsRoute

@Serializable
data object TestMenuRoute

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val onboardingRepository: OnboardingRepository = koinInject()
    var startDestination by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(onboardingRepository) {
        val seen = onboardingRepository.onboardingSeen.first()
        startDestination = if (seen) HomeRoute else OnboardingRoute
    }

    val destination = startDestination
    if (destination == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .then(modifier),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = destination,
        modifier = Modifier.fillMaxSize().then(modifier),
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(HomeRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                },
            )
        }
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
                onOpenTestMenu = { navController.navigate(TestMenuRoute) },
            )
        }
        composable<TestMenuRoute> {
            TestMenuScreen(
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

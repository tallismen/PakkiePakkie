package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieTopBar
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.test_menu_onboarding_seen
import pakkiepakkie.shared.generated.resources.test_menu_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestMenuScreen(
    onBack: () -> Unit,
    viewModel: TestMenuViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TestMenuContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestMenuContent(
    state: TestMenuState,
    onEvent: (TestMenuEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal,
        ),
        topBar = {
            PakkiePakkieTopBar(
                title = stringResource(Res.string.test_menu_title),
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.test_menu_onboarding_seen),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                )
                Switch(
                    checked = state.onboardingSeen,
                    onCheckedChange = { onEvent(TestMenuEvent.OnOnboardingSeenChanged(it)) },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TestMenuScreenPreview() {
    PreviewContainer {
        TestMenuContent(
            state = TestMenuState(onboardingSeen = true),
            onEvent = {},
            onBack = {},
        )
    }
}

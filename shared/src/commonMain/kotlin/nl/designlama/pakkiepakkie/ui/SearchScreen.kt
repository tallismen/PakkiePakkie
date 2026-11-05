package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.ui.components.DutchLicensePlateInput
import nl.designlama.pakkiepakkie.ui.components.LicensePlateInputStyle
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.UserAvatar
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.action_back
import pakkiepakkie.shared.generated.resources.home_empty_search_prompt
import pakkiepakkie.shared.generated.resources.home_profile_placeholder

@Composable
fun SearchScreen(
    onOpenVehicleDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.directions.collectLatest { dir ->
            when (dir) {
                is SearchDirections.OpenVehicleDetail -> onOpenVehicleDetail(dir.kenteken)
                SearchDirections.OpenProfile -> onOpenProfile()
            }
        }
    }
    SearchContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun SearchContent(
    state: SearchState,
    onEvent: (SearchEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                label = stringResource(Res.string.home_profile_placeholder),
                size = 40.dp,
                onClick = { onEvent(SearchEvent.OnProfileClick) },
            )
        }

        DutchLicensePlateInput(
            value = state.licensePlateInput,
            onValueChange = { raw, _ -> onEvent(SearchEvent.OnLicensePlateChange(raw)) },
            showClearButton = true,
            style = LicensePlateInputStyle.Search,
            onDone = { if (state.canSearch) onEvent(SearchEvent.OnSearchClick) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp)
                    .size(28.dp),
                strokeWidth = 3.dp,
            )
        }

        state.errorMessage?.let { msg ->
            PakkiePakkieText(
                text = msg,
                textColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        if (state.licensePlateInput.isEmpty() && !state.loading && state.errorMessage == null) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(Res.string.home_empty_search_prompt),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.action_back),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
                .clickable(onClick = onBack),
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchScreenPreview() {
    PreviewContainer {
        SearchContent(
            state = SearchState(),
            onEvent = {},
            onBack = {},
        )
    }
}

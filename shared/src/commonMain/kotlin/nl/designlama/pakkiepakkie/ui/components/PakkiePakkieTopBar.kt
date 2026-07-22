package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.nav_back_symbol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PakkiePakkieTopBar(
    title: String = "",
    modifier: Modifier = Modifier,
    brandedTitle: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    titleContent: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Text(
                            text = stringResource(Res.string.nav_back_symbol),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
            title = {
                when {
                    titleContent != null -> titleContent()
                    brandedTitle -> {
                        PakkiePakkieText(
                            text = title,
                            textColor = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            singleLine = true,
                        )
                    }
                    else -> {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background,
            ),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

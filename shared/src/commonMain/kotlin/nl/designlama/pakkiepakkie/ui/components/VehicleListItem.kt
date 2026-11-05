package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.designlama.pakkiepakkie.theme.LicensePlateBlue
import org.jetbrains.compose.resources.stringResource
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.home_list_item_subtitle
import pakkiepakkie.shared.generated.resources.home_show_less
import pakkiepakkie.shared.generated.resources.home_show_more
import pakkiepakkie.shared.generated.resources.format_power_pk

data class VehicleListItemModel(
    val kenteken: String,
    val title: String,
    val powerPk: Int?,
    val winPercent: Float?,
    val isFavorite: Boolean,
)

@Composable
fun VehicleListItem(
    model: VehicleListItemModel,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = stringResource(
        Res.string.home_list_item_subtitle,
        formatLicensePlate(model.kenteken),
        model.powerPk?.let { stringResource(Res.string.format_power_pk, it) } ?: "—",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PakkiePakkieGauge(
            percent = model.winPercent,
            sizeDp = 48f,
            showLabel = false,
            animationEnabled = false,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onFavoriteClick) {
            Text(
                text = if (model.isFavorite) UiSymbols.STAR_FILLED else UiSymbols.STAR_OUTLINE,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(if (model.isFavorite) 1f else 0.7f),
            )
        }
    }
}

@Composable
fun VehicleListCard(
    title: String,
    items: List<VehicleListItemModel>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onItemClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    collapsedCount: Int = 5,
) {
    val showToggle = items.size > collapsedCount
    val visible = if (!showToggle || expanded) items else items.take(collapsedCount)

    PakkieCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        )
        visible.forEach { item ->
            VehicleListItem(
                model = item,
                onClick = { onItemClick(item.kenteken) },
                onFavoriteClick = { onFavoriteClick(item.kenteken) },
            )
        }
        if (showToggle) {
            TextButton(
                onClick = { onExpandedChange(!expanded) },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(
                        if (expanded) Res.string.home_show_less else Res.string.home_show_more,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LicensePlateBlue,
                )
            }
        }
    }
}

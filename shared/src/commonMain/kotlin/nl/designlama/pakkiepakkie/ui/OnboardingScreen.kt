package nl.designlama.pakkiepakkie.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.ui.components.PakkieAnimations
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.onboarding_close_cd
import pakkiepakkie.shared.generated.resources.onboarding_kies
import pakkiepakkie.shared.generated.resources.onboarding_oordeel
import pakkiepakkie.shared.generated.resources.onboarding_page1_subtitle
import pakkiepakkie.shared.generated.resources.onboarding_page1_title
import pakkiepakkie.shared.generated.resources.onboarding_page2_subtitle
import pakkiepakkie.shared.generated.resources.onboarding_page2_title
import pakkiepakkie.shared.generated.resources.onboarding_page3_subtitle
import pakkiepakkie.shared.generated.resources.onboarding_page3_title
import pakkiepakkie.shared.generated.resources.onboarding_vergelijk

private data class OnboardingPage(
    val titleRes: org.jetbrains.compose.resources.StringResource,
    val subtitleRes: org.jetbrains.compose.resources.StringResource,
    val imageRes: DrawableResource,
)

private val OnboardingPages = listOf(
    OnboardingPage(
        titleRes = Res.string.onboarding_page1_title,
        subtitleRes = Res.string.onboarding_page1_subtitle,
        imageRes = Res.drawable.onboarding_kies,
    ),
    OnboardingPage(
        titleRes = Res.string.onboarding_page2_title,
        subtitleRes = Res.string.onboarding_page2_subtitle,
        imageRes = Res.drawable.onboarding_vergelijk,
    ),
    OnboardingPage(
        titleRes = Res.string.onboarding_page3_title,
        subtitleRes = Res.string.onboarding_page3_subtitle,
        imageRes = Res.drawable.onboarding_oordeel,
    ),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.directions.collectLatest { dir ->
            when (dir) {
                OnboardingDirections.Finish -> onFinished()
            }
        }
    }
    OnboardingContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { state.pageCount })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            AnimatedContent(
                targetState = pagerState.currentPage.coerceIn(0, OnboardingPages.lastIndex),
                transitionSpec = {
                    val forward = targetState > initialState
                    val enterOffset = { width: Int -> if (forward) width / 5 else -width / 5 }
                    val exitOffset = { width: Int -> if (forward) -width / 5 else width / 5 }
                    (
                        slideInHorizontally(
                            animationSpec = PakkieAnimations.tweenSpec(),
                            initialOffsetX = enterOffset,
                        ) + fadeIn(animationSpec = PakkieAnimations.tweenSpec())
                    ) togetherWith (
                        slideOutHorizontally(
                            animationSpec = PakkieAnimations.tweenSpec(),
                            targetOffsetX = exitOffset,
                        ) + fadeOut(animationSpec = PakkieAnimations.tweenSpec())
                    ) using SizeTransform(clip = false, sizeAnimationSpec = { _, _ ->
                        PakkieAnimations.contentSizeSpec()
                    })
                },
                label = "onboardingCopy",
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val pageData = OnboardingPages[page]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(pageData.titleRes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(pageData.subtitleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp),
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                val pageData = OnboardingPages[page]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Image(
                        painter = painterResource(pageData.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        alignment = Alignment.BottomCenter,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            PageIndicator(
                pageCount = state.pageCount,
                currentPage = pagerState.currentPage,
            )
            Spacer(Modifier.height(16.dp))
        }

        val closeCd = stringResource(Res.string.onboarding_close_cd)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 16.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
        ) {
            IconButton(
                onClick = { onEvent(OnboardingEvent.OnClose) },
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = closeCd },
            ) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (active) 8.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        },
                    ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingScreenPreview() {
    PreviewContainer {
        OnboardingContent(
            state = OnboardingState(),
            onEvent = {},
        )
    }
}

package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.unit.IntSize

/** Shared timing so gauge, layout, and chipped UI stay in sync. */
object PakkieAnimations {
    const val DURATION_MS = 260

    val Easing = FastOutSlowInEasing

    fun <T> tweenSpec(): FiniteAnimationSpec<T> =
        tween(durationMillis = DURATION_MS, easing = Easing)

    fun contentSizeSpec(): FiniteAnimationSpec<IntSize> = tweenSpec()

    fun visibilityEnter() =
        expandVertically(animationSpec = tweenSpec()) +
            fadeIn(animationSpec = tweenSpec())

    fun visibilityExit() =
        shrinkVertically(animationSpec = tweenSpec()) +
            fadeOut(animationSpec = tweenSpec())
}

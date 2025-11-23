package com.avito.ui.transition

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

private const val FadeInDuration = 300
private const val FadeInDelay = 120
private const val FadeOutDuration = 200

fun standardFadeIn(): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis = FadeInDuration, delayMillis = FadeInDelay))

fun standardFadeOut(): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = FadeOutDuration))

fun standardOverlayEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(durationMillis = FadeInDuration, delayMillis = FadeInDelay),
        initialOffsetX = { -it / 3 }
    ) + standardFadeIn()

fun standardOverlayExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(durationMillis = FadeOutDuration),
        targetOffsetX = { it / 3 }
    ) + standardFadeOut()

fun standardOverlayPopEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(durationMillis = FadeInDuration, delayMillis = FadeInDelay),
        initialOffsetX = { it / 3 }
    ) + standardFadeIn()

fun standardOverlayPopExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(durationMillis = FadeOutDuration),
        targetOffsetX = { -it / 3 }
    ) + standardFadeOut()

fun standardOverlayTransform(): ContentTransform =
    standardOverlayEnter() togetherWith standardOverlayExit()


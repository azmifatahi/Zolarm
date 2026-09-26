package com.zolarm.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.zolarm.app.R

/**
 * Resolution order:
 *   1. res/drawable/app_logo.png  (drop-in replacement)
 *   2. res/drawable/ic_zolarm_logo.xml (bundled vector fallback)
 */
@Composable
fun ZolarmLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Zolarm logo"
) {
    val context = LocalContext.current
    val resId = remember(context) {
        context.resources
            .getIdentifier("app_logo", "drawable", context.packageName)
            .takeIf { it != 0 }
            ?: R.drawable.ic_zolarm_logo
    }
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

package com.stylingandroid.compose.listdetail

import android.content.res.Configuration
import android.graphics.Rect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import com.stylingandroid.compose.listdetail.ui.theme.ComposeListDetailTheme

internal val list = (1..10).map { "Item $it" }

@Composable
internal fun TestUi(widthDp: Int, foldBounds: Rect? = null) =
    ComposeListDetailTheme {
        ListDetailLayout(
            list = list,
            configuration = Configuration().apply {
                smallestScreenWidthDp = widthDp
                screenWidthDp = widthDp
            },
            windowLayoutInfo = createWindowLayoutInfo(foldBounds)
        ) {
            CreateUi()
        }
    }

@Composable
internal fun TwoPaneScope<String>.CreateUi() {
    List { list, onSelectionChange ->
        LazyColumn {
            for (entry in list) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelectionChange(entry) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = entry)
                    }
                }
            }
        }
    }
    Detail { selection ->
        Text(selection)
    }
}

internal fun createWindowLayoutInfo(foldBounds: Rect?) =
    WindowLayoutInfo.Builder().apply {
        if (foldBounds != null) {
            setDisplayFeatures(
                listOf(
                    FoldingFeature(
                        bounds = foldBounds,
                        type = FoldingFeature.TYPE_FOLD,
                        state = FoldingFeature.STATE_FLAT
                    )
                )
            )
        }
    }.build()

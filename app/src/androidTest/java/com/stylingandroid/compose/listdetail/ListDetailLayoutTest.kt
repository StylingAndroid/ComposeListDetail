package com.stylingandroid.compose.listdetail

import android.graphics.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ListDetailLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun givenASmallScreen_whenTheUIIsGenerated_thenItHasASingleChild() {
        composeTestRule.setContent {
            TestUi(widthDp = 300)
        }
        composeTestRule.onRoot().onChildren().assertCountEquals(1)
    }

    @Test
    fun givenALargeScreen_whenTheUIIsGenerated_thenItHasTwoChildren() {
        composeTestRule.setContent {
            TestUi(widthDp = 600)
        }

        composeTestRule.onRoot().onChildren().assertCountEquals(2)
    }

    @Test
    fun givenAnAsymmetricFold_whenTheUIIsGenerated_thenTheSplitAlignsToTheFold() {
        val foldOffset = 200.dp
        composeTestRule.setContent {
            val offsetPx = LocalDensity.current.run {
                foldOffset.toPx().toInt()
            }
            TestUi(widthDp = 600, foldBounds = Rect(offsetPx, 0, offsetPx, 800))
        }

        composeTestRule.onRoot().onChildren().assertCountEquals(2)
        composeTestRule.onRoot().onChildren()[1].assertLeftPositionInRootIsEqualTo(foldOffset)
    }
}

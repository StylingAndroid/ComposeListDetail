package com.stylingandroid.compose.listdetail

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SplitLayoutTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun givenASplitLayout_whenWeClickEachItem_thenOnlyThatItemDetailIsDisplayed() {
        composeTestRule.setContent {
            TestUi(widthDp = 600)
        }

        val clickableItems = list.toItemsMap(hasClickAction())

        for ((name, action) in clickableItems) {
            action.performClick()
            val detailItems = list.toItemsMap(hasNoClickAction())
            detailItems.isOnlyItem(name)
        }
    }

    private fun List<String>.toItemsMap(matcher: SemanticsMatcher) =
        map {
            it to composeTestRule.onNode(hasText(it) and matcher)
        }.toMap()

    private fun Map<String, SemanticsNodeInteraction>.isOnlyItem(text: String) {
        for (item in this) {
            if (item.key == text) {
                item.value.assertExists()
            } else {
                item.value.assertDoesNotExist()
            }
        }
    }
}

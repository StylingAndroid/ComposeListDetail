package com.stylingandroid.compose.listdetail

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import org.junit.Rule
import org.junit.Test

class TwoPageLayoutTest {

    private val width = 300

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun givenATwoPageLayout_whenItIsInitiallyDisplayed_thenItExists() {
        composeTestRule.setContent {
            TestUi(width)
        }

        composeTestRule.onList().assertExists()
    }

    @Test
    fun givenATwoPageLayout_whenItIsInitiallyDisplayed_thenItContainsTenChildren() {
        composeTestRule.setContent {
            TestUi(width)
        }

        composeTestRule.onList().onChildren().assertCountEquals(list.size)
    }

    @Test
    fun givenATwoPageLayout_whenAnItemIsClicked_thenItNoLongerExists() {
        composeTestRule.setContent {
            TestUi(width)
        }

        composeTestRule.onClickableTextItem("Item 1").performClick()

        composeTestRule.onList().assertDoesNotExist()
    }

    @Test
    fun givenATwoPageLayout_whenAnItemIsClicked_thenOnlyTheCorrectItemIsDisplayed() {
        composeTestRule.setContent {
            TestUi(width)
        }

        val listItem = list[3]
        val otherListItem = list[0]

        composeTestRule.onClickableTextItem(listItem).performClick()

        composeTestRule.onStaticTextItem(listItem).assertExists()
        composeTestRule.onStaticTextItem(otherListItem).assertDoesNotExist()
    }

    @ExperimentalTestApi
    @Test
    fun givenATwoPageLayout_whenAnItemIsClickedAndBackPressed_thenTheListExists() {
        composeTestRule.setContent {
            TestUi(width)
        }

        val listItem = list[3]

        composeTestRule.onList().assertExists()
        composeTestRule.onList().onChildren().assertCountEquals(10)

        composeTestRule.onClickableTextItem(listItem).performClick()
        Espresso.pressBack()

        composeTestRule.onList().assertExists()
    }
}

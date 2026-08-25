package com.company.cavitrack

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.company.cavitrack.presentation.components.StatusBadge
import com.company.cavitrack.presentation.components.StatusType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statusBadgeDisplaysCorrectText() {
        composeTestRule.setContent {
            StatusBadge(text = "100 pcs", statusType = StatusType.SUCCESS)
        }

        composeTestRule.onNodeWithText("100 pcs").assertIsDisplayed()
    }
}

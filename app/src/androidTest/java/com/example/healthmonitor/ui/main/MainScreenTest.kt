package com.example.healthmonitor.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.healthmonitor.data.HealthData
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.healthmonitor.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { MainScreen(FAKE_DATA) }
  }

  @Test
  fun elements_exist() {
    composeTestRule.onNodeWithText("Health Dashboard").assertExists()
    composeTestRule.onNodeWithText("72 BPM").assertExists()
    composeTestRule.onNodeWithText("120/80 mmHg").assertExists()
  }
}

private val FAKE_DATA = HealthData(72, 120, 80, 8432, 7, 20, "Low", "Normal")

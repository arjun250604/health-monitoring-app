package com.example.healthmonitor.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import com.example.healthmonitor.data.HealthConnectManager
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.healthmonitor.data.DefaultDataRepository
import com.example.healthmonitor.data.HealthData
import com.example.healthmonitor.theme.BloodPressureBgDark
import com.example.healthmonitor.theme.BloodPressureBgLight
import com.example.healthmonitor.theme.BloodPressureColor
import com.example.healthmonitor.theme.HealthMonitorTheme
import com.example.healthmonitor.theme.HeartRateBgDark
import com.example.healthmonitor.theme.HeartRateBgLight
import com.example.healthmonitor.theme.HeartRateColor
import com.example.healthmonitor.theme.HeroGradientEnd
import com.example.healthmonitor.theme.HeroGradientStart
import com.example.healthmonitor.theme.SleepBgDark
import com.example.healthmonitor.theme.SleepBgLight
import com.example.healthmonitor.theme.SleepColor
import com.example.healthmonitor.theme.StatusHighRed
import com.example.healthmonitor.theme.StatusLowGreen
import com.example.healthmonitor.theme.StatusModerateOrange
import com.example.healthmonitor.theme.StepsBgDark
import com.example.healthmonitor.theme.StepsBgLight
import com.example.healthmonitor.theme.StepsColor

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  var showInputForm by remember { mutableStateOf(false) }
  
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val healthConnectManager = remember { HealthConnectManager(context) }
  
  val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
  val requestPermissions = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
      if (granted.containsAll(healthConnectManager.permissions)) {
          coroutineScope.launch {
              val healthData = healthConnectManager.readLatestVitals()
              if (healthData != null) {
                  viewModel.updateVitals(
                      healthData.heartRate,
                      healthData.systolicBp,
                      healthData.diastolicBp,
                      healthData.steps,
                      healthData.sleepHours,
                      healthData.sleepMinutes
                  )
              }
          }
      }
  }

  val onSyncWearable: () -> Unit = {
      coroutineScope.launch {
          if (healthConnectManager.hasAllPermissions()) {
              val healthData = healthConnectManager.readLatestVitals()
              if (healthData != null) {
                  viewModel.updateVitals(
                      healthData.heartRate,
                      healthData.systolicBp,
                      healthData.diastolicBp,
                      healthData.steps,
                      healthData.sleepHours,
                      healthData.sleepMinutes
                  )
              }
          } else {
              requestPermissions.launch(healthConnectManager.permissions)
          }
      }
  }

  androidx.compose.material3.Scaffold(
      floatingActionButton = {
          androidx.compose.material3.FloatingActionButton(onClick = { showInputForm = true }) {
              Icon(Icons.Default.Add, contentDescription = "Add Vitals")
          }
      }
  ) { paddingValues ->
      Box(modifier = modifier.padding(paddingValues).fillMaxSize()) {
          when (state) {
            MainScreenUiState.Empty -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No Data",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(
                    text = "No vitals data yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Tap the + button to add.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(24.dp))
                  Button(onClick = onSyncWearable) {
                    Icon(Icons.Default.Watch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync with Wearable")
                  }
                }
              }
            }
            MainScreenUiState.Loading -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "Loading Vitals...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
            is MainScreenUiState.Success -> {
              MainScreen(
                  data = (state as MainScreenUiState.Success).data, 
                  onSyncWearable = onSyncWearable,
                  modifier = Modifier
              )
            }
            is MainScreenUiState.Error -> {
              Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                  shape = RoundedCornerShape(16.dp)
                ) {
                  Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Warning,
                      contentDescription = "Error",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                      "Error loading data: ${(state as MainScreenUiState.Error).throwable.message}",
                      color = MaterialTheme.colorScheme.onErrorContainer,
                      style = MaterialTheme.typography.bodyMedium
                    )
                  }
                }
              }
            }
          }
      }
  }

  if (showInputForm) {
      VitalsInputDialog(
          onDismiss = { showInputForm = false },
          onSubmit = { hr, sysBp, diaBp, steps, slpH, slpM ->
              viewModel.updateVitals(hr, sysBp, diaBp, steps, slpH, slpM)
              showInputForm = false
          }
      )
  }
}

@Composable
internal fun MainScreen(data: HealthData, onSyncWearable: () -> Unit = {}, modifier: Modifier = Modifier) {
  val scrollState = rememberScrollState()
  val isDark = isSystemInDarkTheme()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    // Top Bar Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Health Dashboard",
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Manual Mode",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Manual Entry Mode",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Quick Status Badge
      OutlinedButton(onClick = onSyncWearable) {
          Icon(Icons.Default.Watch, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sync", style = MaterialTheme.typography.labelSmall)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Summary Hero Banner
    HeroSummaryBanner(data = data)

    Spacer(modifier = Modifier.height(20.dp))

    // Section Title
    Text(
      text = "Vital Metrics",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Vital Cards Row 1
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      DashboardCard(
        title = "Heart Rate",
        value = "${data.heartRate} BPM",
        subtitle = "60-100 Normal range",
        icon = Icons.Default.Favorite,
        iconTint = HeartRateColor,
        iconBgColor = if (isDark) HeartRateBgDark else HeartRateBgLight,
        modifier = Modifier.weight(1f)
      )
      DashboardCard(
        title = "Blood Pressure",
        value = "${data.systolicBp}/${data.diastolicBp} mmHg",
        subtitle = "Systolic/Diastolic",
        icon = Icons.Default.Speed,
        iconTint = BloodPressureColor,
        iconBgColor = if (isDark) BloodPressureBgDark else BloodPressureBgLight,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Vital Cards Row 2
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      DashboardCard(
        title = "Steps",
        value = "${data.steps}",
        subtitle = "Goal: 10,000 steps",
        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
        iconTint = StepsColor,
        iconBgColor = if (isDark) StepsBgDark else StepsBgLight,
        progress = (data.steps / 10000f).coerceIn(0f, 1f),
        modifier = Modifier.weight(1f)
      )
      DashboardCard(
        title = "Sleep",
        value = "${data.sleepHours}h ${data.sleepMinutes}m",
        subtitle = "Quality Rest",
        icon = Icons.Default.Bedtime,
        iconTint = SleepColor,
        iconBgColor = if (isDark) SleepBgDark else SleepBgLight,
        progress = ((data.sleepHours * 60 + data.sleepMinutes) / 480f).coerceIn(0f, 1f),
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ML Predictions Section Header with AI Badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("ML Predictions", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "AI Powered",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // ML Predictions Cards
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      PredictionCard(
        title = "Risk Level",
        value = data.riskLevel,
        icon = Icons.Default.Shield,
        isRisk = true,
        modifier = Modifier.weight(1f)
      )
      PredictionCard(
        title = "Stress Level",
        value = data.stressLevel,
        icon = Icons.Default.Psychology,
        isRisk = false,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun HeroSummaryBanner(data: HealthData, modifier: Modifier = Modifier) {
  val isDark = isSystemInDarkTheme()
  val gradient = Brush.horizontalGradient(
    colors = listOf(HeroGradientStart, HeroGradientEnd)
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(gradient)
        .padding(18.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Vitals Overview",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f)
          )
          Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = CircleShape
          ) {
            Text(
              text = "Good Health",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Column {
            Text(
              text = "All System Normal",
              style = MaterialTheme.typography.headlineMedium,
              color = Color.White
            )
            Text(
              text = "Heart rate, BP and sleep patterns are stable",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        }
      }
    }
  }
}

@Composable
fun DashboardCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector? = null,
  iconTint: Color = MaterialTheme.colorScheme.primary,
  iconBgColor: Color = MaterialTheme.colorScheme.primaryContainer,
  progress: Float? = null,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = CardDefaults.outlinedCardBorder().copy(
      width = 1.dp
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )

        if (icon != null) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(iconBgColor),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = title,
              tint = iconTint,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      if (progress != null) {
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = iconTint,
          trackColor = iconBgColor
        )
      }

      if (subtitle != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun PredictionCard(
  title: String,
  value: String,
  icon: ImageVector,
  isRisk: Boolean,
  modifier: Modifier = Modifier
) {
  val (badgeBg, badgeTextColor) = when (value.lowercase()) {
    "low", "normal" -> StatusLowGreen.copy(alpha = 0.15f) to StatusLowGreen
    "medium", "moderate" -> StatusModerateOrange.copy(alpha = 0.15f) to StatusModerateOrange
    "high" -> StatusHighRed.copy(alpha = 0.15f) to StatusHighRed
    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
  }

  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = badgeTextColor,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Surface(
        color = badgeBg,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = value,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = badgeTextColor,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
fun VitalsInputDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, Int, Int, Int, Int, Int) -> Unit
) {
    var heartRate by remember { mutableStateOf("") }
    var systolicBp by remember { mutableStateOf("") }
    var diastolicBp by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var sleepHours by remember { mutableStateOf("") }
    var sleepMinutes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Vitals") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = heartRate, onValueChange = { heartRate = it }, label = { Text("Heart Rate (BPM)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = systolicBp, onValueChange = { systolicBp = it }, label = { Text("Systolic BP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = diastolicBp, onValueChange = { diastolicBp = it }, label = { Text("Diastolic BP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = steps, onValueChange = { steps = it }, label = { Text("Steps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = sleepHours, onValueChange = { sleepHours = it }, label = { Text("Sleep Hours") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = sleepMinutes, onValueChange = { sleepMinutes = it }, label = { Text("Sleep Minutes") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(
                    heartRate.toIntOrNull() ?: 0,
                    systolicBp.toIntOrNull() ?: 0,
                    diastolicBp.toIntOrNull() ?: 0,
                    steps.toIntOrNull() ?: 0,
                    sleepHours.toIntOrNull() ?: 0,
                    sleepMinutes.toIntOrNull() ?: 0
                )
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

val sampleData = HealthData(72, 120, 80, 8432, 7, 20, "Low", "Normal")

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  HealthMonitorTheme { MainScreen(sampleData) }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun MainScreenPortraitPreview() {
  HealthMonitorTheme { MainScreen(sampleData) }
}

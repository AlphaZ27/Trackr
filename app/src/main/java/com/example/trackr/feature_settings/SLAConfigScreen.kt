package com.example.trackr.feature_settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.SLARule
import com.example.trackr.feature_tickets.ui.shared.toColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SLAConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: SLAConfigViewModel = hiltViewModel()
) {
    val rules by viewModel.slaRules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SLA Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                "Set resolution time targets (in hours) for each priority level.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rules.sortedByDescending { it.priority.ordinal }) { rule ->
                    SLARuleItem(
                        rule = rule,
                        onValueChange = { newHours -> viewModel.updateRule(rule, newHours) }
                    )
                }
            }
        }
    }
}

@Composable
fun SLARuleItem(
    rule: SLARule,
    onValueChange: (Int) -> Unit
) {
    var sliderValue by remember(rule.maxResolutionTimeHours) { mutableFloatStateOf(rule.maxResolutionTimeHours.toFloat()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rule.priority.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = rule.priority.toColor()
                )
                Text(
                    text = "${sliderValue.toInt()} Hour(s)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onValueChange(sliderValue.toInt()) },
                valueRange = 1f..72f, // 1 hour to 3 days
                steps = 71
            )

            Text(
                text = "Warning trigger: ${(sliderValue * 0.8).toInt()} hour(s)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
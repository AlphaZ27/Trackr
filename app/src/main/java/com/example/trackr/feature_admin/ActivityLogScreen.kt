package com.example.trackr.feature_admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.trackr.domain.model.ActivityLog
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActivityLogViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Activity Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.Green,
                    navigationIconContentColor = Color.Green
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Terminal Background
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs) { log ->
                    TerminalLogItem(log)
                }
            }
        }
    }
}

@Composable
fun TerminalLogItem(log: ActivityLog) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.US) }
    val dateStr = log.timestamp?.toDate()?.let { dateFormat.format(it) } ?: "PENDING..."

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "[$dateStr] ${log.action}",
            color = Color(0xFF00FF00), // Terminal Green
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )
        Text(
            text = " > ${log.details} [User: ${log.performedBy}]",
            color = Color(0xFF00AA00), // Slightly dimmer green
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
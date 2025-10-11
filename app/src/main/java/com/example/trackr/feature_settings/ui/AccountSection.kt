package com.example.trackr.feature_settings.ui


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackr.feature_settings.domain.model.UserType

@Composable
fun AccountSection(
    userType: UserType,
    onLogoutClick: () -> Unit,
    onDashboardClick: (UserType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Using SectionHeader for consistency
        com.example.trackr.feature_settings.ui.components.SectionHeader(title = "Account")

        Text(
            text = "Current Role: ${userType.name}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onDashboardClick(userType) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Go to Dashboard")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Logout")
        }
    }
}
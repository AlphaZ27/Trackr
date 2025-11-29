package com.example.trackr.domain.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a rule for a specific Priority.
 * e.g., Priority.High should be resolved within 8 hours.
 */
data class SLARule(
    @DocumentId val id: String = "", // Typically the priority name (e.g., "High")
    val priority: Priority = Priority.Low,
    val maxResolutionTimeHours: Int = 24, // Time until "Breached"
    val warningThresholdHours: Int = 20   // Time until "Warning"
)
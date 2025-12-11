package com.example.trackr.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

enum class ArticleStatus {
    Draft, Published, Archived
}

data class KBArticle(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val content: String = "",
//    val attachments: List<String> = emptyList(), // Can add this in a later phase
    val status: ArticleStatus = ArticleStatus.Draft,
    val authorId: String = "",
    val createdBy: String = "",
    val reviewerId: String? = null,
    @ServerTimestamp val lastUpdated: Timestamp? = null,

    // Engagement Metrics
    val relatedTickets: List<String> = emptyList(),
    val viewCount: Int = 0,
    val helpfulCount: Int = 0,
    val unhelpfulCount: Int = 0,
    @ServerTimestamp val lastViewedAt: Timestamp? = null,

    // AI & Search Optimization
    val keywords: List<String> = emptyList(), // Specific keywords for matching tickets
    val searchMatches: Int = 0, // How many times this appeared in search results

    // Quality & Linking
    val qualityScore: Double = 0.0 // Calculated score (views vs helpfulness)
)
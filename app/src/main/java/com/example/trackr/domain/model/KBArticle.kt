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
//    val attachments: List<String> = emptyList(), // We can add this in a later phase
    val status: ArticleStatus = ArticleStatus.Draft,
    val authorId: String = "",
    val reviewerId: String? = null,
    @ServerTimestamp val lastUpdated: Timestamp? = null,
    val relatedTickets: List<String> = emptyList(),
    val viewCount: Int = 0
)
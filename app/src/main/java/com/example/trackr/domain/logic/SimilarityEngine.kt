package com.example.trackr.domain.logic

import com.example.trackr.domain.model.KBArticle
import com.example.trackr.domain.model.Ticket
import java.util.Locale
import javax.inject.Inject

// Jaccard Similarity is a measure of how similar two sets are.
// It will calculate how similar two tickets or an article is

class SimilarityEngine @Inject constructor() {

    /**
     * Calculates a similarity score (0.0 to 1.0) between a query and a target text.
     * Uses a simple keyword overlap strategy.
     */
    fun calculateSimilarity(query: String, target: String): Double {
        val queryKeywords = extractKeywords(query)
        val targetKeywords = extractKeywords(target)

        if (queryKeywords.isEmpty() || targetKeywords.isEmpty()) return 0.0

        val intersection = queryKeywords.intersect(targetKeywords).size
        val union = queryKeywords.union(targetKeywords).size

        if (union == 0) return 0.0

        // Jaccard Index: Intersection / Union
        return intersection.toDouble() / union.toDouble()
    }

    /**
     * Finds KB Articles that match the given ticket description.
     * Returns articles with a score > threshold, sorted by score.
     */
    fun findRelevantArticles(
        query: String,
        articles: List<KBArticle>,
        threshold: Double = 0.1 // Low threshold for suggestions
    ): List<KBArticle> {
        return articles.map { article ->
            // Compare query against both Title and Content
            val titleScore = calculateSimilarity(query, article.title)
            val contentScore = calculateSimilarity(query, article.content)
            val totalScore = (titleScore * 0.7) + (contentScore * 0.3) // Weight title higher

            article to totalScore
        }
            .filter { it.second >= threshold }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    /**
     * Helper to tokenize text into meaningful keywords.
     */
    private fun extractKeywords(text: String): Set<String> {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9 ]"), "") // Remove punctuation
            .split(" ")
            .filter { it.length > 3 } // Filter out short words (is, the, and)
            .toSet()
    }
}
package com.example.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchConstitutionRequest(
    @SerialName("query_embedding") val queryEmbedding: List<Float>,
    @SerialName("match_threshold") val matchThreshold: Float,
    @SerialName("match_count") val matchCount: Int
)

@Serializable
data class SearchConstitutionResponse(
    @SerialName("chunk_id") val chunkId: String,
    @SerialName("document_id") val documentId: String,
    val content: String,
    @SerialName("article_ref") val articleRef: String?,
    @SerialName("section_ref") val sectionRef: String?,
    val similarity: Float
)

package com.example.ngilizcequizapp

import kotlinx.serialization.Serializable

@Serializable
data class QuizContent(
    val questions: List<RemoteQuestion>
)

@Serializable
data class RemoteQuestion(
    val id: Int,
    val type: String,
    val text: String,
    val image_url: String,
    val answer: String,
    val options: List<String>,
    val translation: String? = null
)
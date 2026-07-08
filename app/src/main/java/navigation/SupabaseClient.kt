package com.example.ngilizcequizapp

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://qtbqsbmlnrncnkijaifn.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF0YnFzYm1sbnJuY25raWphaWZuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODM1MjUyODIsImV4cCI6MjA5OTEwMTI4Mn0.ekEF8HhkkjcTHdWN0chMUOhJBl-4HVjZZLFlqyPobsw"
    ) {
        install(Postgrest)
    }
}

@Serializable
data class UserProfile(
    val id: String? = null,
    val name: String,
    val surname: String,
    val age: Int
)

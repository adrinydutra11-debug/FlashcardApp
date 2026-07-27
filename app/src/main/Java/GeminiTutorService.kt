package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun getTutorResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(), // Pair(role "user"/"model", text)
        systemPrompt: String = "Você é uma Tutora IA educacional simpática, paciente e altamente didática para um aplicativo de estudo de flashcards. Responda em Português do Brasil com clareza, formatação organizada, dicas mnemônicas e exemplos práticos. Ajude o estudante a tirar dúvidas, treinar conversação e entender matérias difíceis."
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "A chave API do Gemini não foi configurada. Configure a chave no painel de Segredos (Secrets Panel) no AI Studio para usar a Tutora IA em tempo real!"
        }

        val contents = mutableListOf<GeminiContent>()

        // Add history turns
        for (turn in history) {
            val roleName = if (turn.first == "user") "user" else "model"
            contents.add(
                GeminiContent(
                    role = roleName,
                    parts = listOf(GeminiPart(text = turn.second))
                )
            )
        }

        // Add current prompt
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        val sysInstruction = GeminiContent(
            parts = listOf(GeminiPart(text = systemPrompt))
        )

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = sysInstruction
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val replyText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            replyText ?: "Não foi possível obter resposta da Tutora IA no momento. Tente novamente."
        } catch (e: Exception) {
            "Erro ao conectar com a Tutora IA: ${e.localizedMessage ?: "Verifique sua conexão com a internet."}"
        }
    }
}

package com.aliucord.plugins.translate

data class TranslateData(
        val sourceLanguage: String,
        val translatedLanguage: String,
        val sourceText: String,
        val translatedText: String
)

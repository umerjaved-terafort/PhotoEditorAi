package com.itg.template.ui.component.language.data

import android.content.res.Resources
import android.os.Build
import com.itg.template.R

object LanguageData {

    private val _languages = mutableListOf(
        LanguageModel(name = "English", iso = "en", imageResId = R.drawable.ic_english, nativeName = "English"),
        LanguageModel(name = "Spanish", iso = "es", imageResId = R.drawable.ic_spanish, nativeName = "Español"),
        LanguageModel(name = "French", iso = "fr", imageResId = R.drawable.ic_france, nativeName = "Français"),
        LanguageModel(name = "Arabic", iso = "ar", imageResId = R.drawable.ic_arabic, nativeName = "العربية"),
        LanguageModel(name = "Hindi", iso = "hi", imageResId = R.drawable.ic_hindi, nativeName = "हिन्दी"),
        LanguageModel(name = "German", iso = "de", imageResId = R.drawable.ic_german, nativeName = "Deutsch"),
        LanguageModel(name = "Portuguese", iso = "pt", imageResId = R.drawable.ic_portugal, nativeName = "Português"),
        LanguageModel(name = "Indonesian", iso = "in", imageResId = R.drawable.ic_indonesian, nativeName = "Bahasa Indonesia"),
        LanguageModel(name = "Urdu", iso = "ur", imageResId = R.drawable.ic_urdu, nativeName = "اردو"),
        LanguageModel(name = "Turkish", iso = "tr", imageResId = R.drawable.ic_turkish, nativeName = "Türkçe"),
        LanguageModel(name = "Croatian", iso = "hr", imageResId = R.drawable.ic_croatia, nativeName = "Hrvatski"),
        LanguageModel(name = "Czech", iso = "cs", imageResId = R.drawable.ic_czech_republic, nativeName = "Čeština"),
        LanguageModel(name = "Dutch", iso = "nl", imageResId = R.drawable.ic_dutch, nativeName = "Nederlands"),
        LanguageModel(name = "Filipino", iso = "fil", imageResId = R.drawable.ic_filipino, nativeName = "Filipino"),
        LanguageModel(name = "Italian", iso = "it", imageResId = R.drawable.ic_italian, nativeName = "Italiano"),
        LanguageModel(name = "Japanese", iso = "ja", imageResId = R.drawable.ic_japanese, nativeName = "日本語"),
        LanguageModel(name = "Korean", iso = "ko", imageResId = R.drawable.ic_korean, nativeName = "한국어"),
        LanguageModel(name = "Malay", iso = "ms", imageResId = R.drawable.ic_malay, nativeName = "Bahasa Melayu"),
        LanguageModel(name = "Polish", iso = "pl", imageResId = R.drawable.ic_polish, nativeName = "Polski"),
        LanguageModel(name = "Russian", iso = "ru", imageResId = R.drawable.ic_russian, nativeName = "Русский"),
        LanguageModel(name = "Serbian", iso = "sr", imageResId = R.drawable.ic_serbian, nativeName = "Српски"),
        LanguageModel(name = "Swedish", iso = "sv", imageResId = R.drawable.ic_swedish, nativeName = "Svenska"),
        LanguageModel(name = "Vietnamese", iso = "vi", imageResId = R.drawable.ic_vietnamese, nativeName = "Tiếng Việt"),
        LanguageModel(name = "China", iso = "zh", imageResId = R.drawable.ic_china, nativeName = "中文")
    )


    val defaultLanguage: LanguageModel
        get() = _languages.first()

    val systemLanguage: LanguageModel
        get() = run {
            val systemIso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales[0].language
            } else {
                Resources.getSystem().configuration.locale.language
            }
            _languages.find { it.iso == systemIso } ?: defaultLanguage
        }

    val languages: List<LanguageModel>
        get() = run {
            val result = _languages.take(10).toMutableList()
            if (result.contains(systemLanguage).not()) {
                result.add(0, systemLanguage)
            } else {
                result.remove(systemLanguage)
                result.add(0, systemLanguage)
            }
            result
        }


    fun selectLanguage(iso: String) {
        val result = mutableListOf<LanguageModel>()
        _languages.forEach {
            if (it.iso == iso) {
                result.add(it.copy(selected = true))
            } else {
                result.add(it.copy(selected = false))
            }
        }
        _languages.clear()
        _languages.addAll(result)
    }
}

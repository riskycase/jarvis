package com.riskycase.jarvis

data class Filter(val title: String, val text: String) {
    override fun toString(): String {
        return "Title: $title, Text: $text"
    }
}

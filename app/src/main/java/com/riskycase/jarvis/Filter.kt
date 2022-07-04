package com.riskycase.jarvis

data class Filter(val title: Match, val text: Match) {
    override fun toString(): String {
        return "Title: $title, Text: $text"
    }
}

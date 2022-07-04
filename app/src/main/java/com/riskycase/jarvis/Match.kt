package com.riskycase.jarvis

data class Match(val string: String, val type: Enum<MatchType>) {
    enum class MatchType{
        EXACT,
        CONTAINS,
        EXTRACT
    }

    fun getTypeInt(): Int {
        return if (this.type == MatchType.EXTRACT)
            0
        else if(this.type == MatchType.CONTAINS)
            1
        else
            2
    }

    override fun toString(): String {
        return "${if(type == MatchType.CONTAINS) "Contains" else if(type == MatchType.EXACT) "Exactly" else "Extract"} $string"
    }
}
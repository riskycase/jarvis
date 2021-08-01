package com.riskycase.jarvis

import java.util.*

data class Snap(val key: String, val sender: String, val time: Long){
    private var _key: String = key
    private var _sender: String = sender
    private var _time: Date = Date(time)

    fun setKey(key: String): Snap {
        this._key = key
        return this
    }

    @JvmName("getKey1")
    fun getKey(): String {
        return this._key
    }

    fun setSender(sender: String): Snap {
        this._sender = sender
        return this
    }

    @JvmName("getSender1")
    fun getSender(): String {
        return this._sender
    }

    fun setTime(time: Long): Snap {
        this._time = Date(time)
        return this
    }

    @JvmName("getTime1")
    fun getTime(): Long {
        return this._time.time
    }

    override fun toString(): String {
        return _sender.plus(" | ").plus(_time)
    }
}

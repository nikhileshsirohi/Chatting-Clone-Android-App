package com.example.chattingclone.Data

open class Event<out T>(val content: T) {
    var hasBeenHandle = false
    fun getContentOrNull(): T?{
        return if(hasBeenHandle) null
        else{
            hasBeenHandle = true
            content
        }
    }
}
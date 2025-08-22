package com.myapp.backend.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    fun info(message: String) {
        log("INFO", message)
    }
    
    fun warning(message: String) {
        log("WARNING", message)
    }
    
    fun error(message: String) {
        log("ERROR", message)
    }
    
    fun debug(message: String) {
        log("DEBUG", message)
    }
    
    private fun log(level: String, message: String) {
        val timestamp = LocalDateTime.now().format(formatter)
        println("[$timestamp] [$level] $message")
    }
}

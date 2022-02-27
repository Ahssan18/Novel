package com.example.novel.util

public data class Novel(var string: String = "Page:", var id: Int) {
    override fun toString(): String {
        return "$string  $id"
    }
}
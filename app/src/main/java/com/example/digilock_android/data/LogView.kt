package com.example.digilock_android.data

import android.annotation.SuppressLint
import android.util.Log
import android.widget.TextView

class LogView(private val view: TextView) {

    @SuppressLint("SetTextI18n")
    fun logPrepend(text: String, tag: String) {
        Log.d(tag, text)
        this.view.text = "$line: $text\n${this.view.text}"
        line++
    }

    fun logAppend(text: String, tag: String) {
        Log.d(tag, text)
        this.view.append("$line: $text\n")
        line++
    }

    companion object {
        private var line = 0
    }
}
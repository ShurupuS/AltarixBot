package me.ilich.bigbrother

import java.io.File

interface MessagePresenter {
    fun displayText(text: String)
    fun displayImageFromFile(imageFile: File, userName: String?)
    fun displayTextWithClarification(text: String, userName: String?)
}
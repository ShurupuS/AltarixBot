package me.ilich.bigbrother

import java.io.File

interface MessagePresenter {
    fun displayText(text: String)
    fun displayImageFromFile(imageFile: File)
    fun displayTextWithClarification(text: String)
}
package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter
import java.io.File

class ImageMessage(id: String, val imageFile: File, status: String) : Message(id, status) {

    override fun display(presenter: MessagePresenter) {
        presenter.displayImageFromFile(imageFile)
    }

}
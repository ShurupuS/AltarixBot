package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter
import java.io.File

class ImageMessage(val imageFile: File) : Message() {

    override fun display(presenter: MessagePresenter) {
        presenter.displayImageFromFile(imageFile)
    }

}
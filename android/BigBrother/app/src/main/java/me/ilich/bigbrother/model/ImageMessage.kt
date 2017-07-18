package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter
import java.io.File

class ImageMessage(id: String, val imageFile: File, status: String, userName: String?) : Message(id, status, userName) {

    override fun display(presenter: MessagePresenter) {
        presenter.displayImageFromFile(imageFile, userName)
    }

}
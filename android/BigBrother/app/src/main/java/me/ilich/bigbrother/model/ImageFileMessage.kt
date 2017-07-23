package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter
import java.io.File

class ImageFileMessage(id: String, val imageFile: File, status: String, userName: String?) : Message(id, status, userName) {

    override fun display(presenter: MessagePresenter) {
        presenter.mode(MessagePresenter.Mode.IMAGE)
        //TODO
    }

}
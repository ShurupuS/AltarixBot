package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

class ImageUrlMessage(id: String, val imageUrl: String, status: String, userName: String?) : Message(id, status, userName) {

    override fun display(presenter: MessagePresenter) {
        presenter.messageMode(MessagePresenter.MessageMode.IMAGE)
        presenter.messageImageUrl(imageUrl)
    }

}
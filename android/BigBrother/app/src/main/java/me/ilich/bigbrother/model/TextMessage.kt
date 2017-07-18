package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

class TextMessage(id: String, val text: String, status: String, userName: String?) : Message(id, status, userName) {

    override fun display(presenter: MessagePresenter) {
        presenter.displayTextWithClarification(text, userName)
    }

}
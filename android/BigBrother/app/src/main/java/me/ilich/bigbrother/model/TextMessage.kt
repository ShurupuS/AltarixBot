package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

class TextMessage(id: String, val text: String, status: String) : Message(id, status) {

    override fun display(presenter: MessagePresenter) {
        presenter.displayTextWithClarification(text)
    }

}
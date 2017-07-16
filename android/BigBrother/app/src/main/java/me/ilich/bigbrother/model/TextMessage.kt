package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

class TextMessage(val text: String) : Message() {

    override fun display(presenter: MessagePresenter) {
        presenter.displayText(text)
    }

}
package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

class StubMessage : Message("stub", "") {

    override fun display(presenter: MessagePresenter) {
        presenter.displayText("@AltarixBot")
    }

}
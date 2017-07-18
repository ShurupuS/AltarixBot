package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

class UnknownMessage : Message("unknown", "", null) {
    override fun display(presenter: MessagePresenter) {}
}
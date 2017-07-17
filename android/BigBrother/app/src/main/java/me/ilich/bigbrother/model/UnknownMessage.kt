package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

class UnknownMessage : Message("unknown", "") {
    override fun display(presenter: MessagePresenter) {}
}
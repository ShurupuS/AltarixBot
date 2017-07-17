package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

abstract class Message(
        val id: String,
        val status: String
) {

    abstract fun display(presenter: MessagePresenter)

}
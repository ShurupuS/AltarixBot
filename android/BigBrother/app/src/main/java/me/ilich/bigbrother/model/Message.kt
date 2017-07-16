package me.ilich.bigbrother.model

import me.ilich.bigbrother.MessagePresenter

abstract class Message {

    abstract fun display(presenter: MessagePresenter)

}
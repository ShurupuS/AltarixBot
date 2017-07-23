package me.ilich.bigbrother

interface MessagePresenter {

    fun messageMode(mode: MessageMode)
    fun messageText(message: String)
    fun messageImageUrl(imageUrl: String)
    fun timerMode(mode: TimerMode)
    fun timer(seconds: Long)
    fun userName(userName: List<String>)

    enum class MessageMode {
        TEXT, IMAGE, CLARIFICATION
    }

    enum class TimerMode {
        ON, OFF
    }

}
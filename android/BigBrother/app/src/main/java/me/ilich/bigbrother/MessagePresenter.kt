package me.ilich.bigbrother

interface MessagePresenter {

    fun mode(mode: Mode)
    fun messageText(message: String)
    fun messageImageUrl(imageUrl: String)
    fun timer(seconds: Long)
    fun userName(userName: List<String>)

    enum class Mode {
        TEXT, IMAGE, CLARIFICATION
    }

}
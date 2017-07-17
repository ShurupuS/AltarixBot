package me.ilich.bigbrother.server.modules

object Status {

    class Response(
            val messages: List<Message>
    ) {

        class Message(
                val id: String,
                val status: String
        )

    }

}
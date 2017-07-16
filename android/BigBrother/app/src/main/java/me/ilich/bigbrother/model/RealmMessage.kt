package me.ilich.bigbrother.model

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.io.File
import java.util.*

@RealmClass open class RealmMessage(
        open var id: String = UUID.randomUUID().toString(),
        open var publishAt: Date = Date(),
        open var type: String = TYPE_TEXT,
        open var text: String? = null,
        open var imageFileName: String? = null
) : RealmObject() {

    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
    }

    fun toMessage(): Message =
            when (type) {
                TYPE_TEXT -> TextMessage(text ?: "")
                TYPE_IMAGE -> ImageMessage(File(imageFileName))
                else -> UnknownMessage()
            }

}
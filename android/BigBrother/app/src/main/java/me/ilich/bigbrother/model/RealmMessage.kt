package me.ilich.bigbrother.model

import io.realm.RealmObject
import java.io.File
import java.util.*

open class RealmMessage(
        open var id: String = UUID.randomUUID().toString(),
        open var publishAt: Date = Date(),
        open var showAt: Date? = null,
        open var hideAt: Date? = null,
        open var type: String = TYPE_TEXT,
        open var status: String = STATUS_PUBLISHED,
        open var text: String? = null,
        open var imageFileName: String? = null,
        open var imageUrl: String? = null,
        open var userName: String? = null
) : RealmObject() {

    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE_FILE = "image_file"
        const val TYPE_IMAGE_URL = "image_url"
        const val STATUS_PUBLISHED = "published"
        const val STATUS_VISIBLE = "visible"
        const val STATUS_FINISHED = "finished"
    }

    fun toMessage(): Message =
            when (type) {
                TYPE_TEXT -> TextMessage(id, text ?: "", status, userName)
                TYPE_IMAGE_FILE -> ImageFileMessage(id, File(imageFileName), status, userName)
                TYPE_IMAGE_URL -> {
                    imageUrl.let { url ->
                        if (url == null) {
                            throw NullPointerException("imageUrl")
                        } else {
                            ImageUrlMessage(id, url, status, userName)
                        }
                    }
                }
                else -> throw RuntimeException("unknown type $type")
            }

}
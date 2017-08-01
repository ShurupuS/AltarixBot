# BigBrother

is watching you

## Tasks

отображение картинок, например https://api.telegram.org/file/bot418853143:AAF6fakwdn_1gQpOYmu-W7kns0fJSh-yB8k/stickers/40599651539223276.webp

## API

GET /publish.php?text=<some_text>&user_name=<user_name>

GET /publish.php?image_url=<image_url>&user_name=<user_name>

{
    "timestamp": <timestamp>,
    "message_id": <message_id>
}

GET /message_status.php

{
    "timestamp": <timestamp>,
    "status": <waiting|visible|past>
}

GET /take_photo.php?callback_url=<callback_url>

{
    "photo_id": <photo_id>
}

callback - методом POST на данный url отправляет фото

GET /photo/<photo_id>


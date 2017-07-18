# BigBrother

is watching you

## API

GET /publish.php?text=<some_text>&user_name=<user_name>

GET /publish.php?image=<image_url>&user_name=<user_name>

{
    "timestamp": <timestamp>,
    "message_id": <message_id>
}

GET /message_status.php

{
    "timestamp": <timestamp>,
    "status": <waiting|visible|past>
}

GET /make_photo.php

{
    "photo_id": <photo_id>
}

GET /photo/<photo_id>


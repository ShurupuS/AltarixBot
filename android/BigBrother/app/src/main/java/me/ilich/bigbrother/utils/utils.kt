package me.ilich.bigbrother.utils

import java.util.*
import java.util.concurrent.TimeUnit

fun Date.add(count: Long, unit: TimeUnit) =
        Date(this.time + unit.toMillis(count))

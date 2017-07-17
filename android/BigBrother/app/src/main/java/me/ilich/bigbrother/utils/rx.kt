package me.ilich.bigbrother.utils

import rx.Observable
import java.util.concurrent.TimeUnit

fun <T> Observable<T>.repeatWithDelay(delay: Long, unit: TimeUnit) =
        repeatWhen { completedObservable ->
            completedObservable.flatMap {
                Observable.timer(delay, unit)
            }
        }

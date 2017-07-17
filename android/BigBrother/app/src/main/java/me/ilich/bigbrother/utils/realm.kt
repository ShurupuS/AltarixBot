package me.ilich.bigbrother.utils

import io.realm.Realm
import rx.Observable

fun Realm.transactionObservable(transaction: (Realm) -> (Unit)): Observable<Unit> =
        Observable.unsafeCreate { subscriber ->
            this.executeTransactionAsync({ realm ->
                transaction(realm)
            }, {
                subscriber.onNext(Unit)
                subscriber.onCompleted()
            }, { th ->
                subscriber.onError(th)
            })
        }

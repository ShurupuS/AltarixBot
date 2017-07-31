package me.ilich.bigbrother

import rx.Observable
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

fun addressBroadcast(): Observable<Unit> =
        Observable.just(Unit)
                .map {
                    DeviceInfo(BigBrotherService.PORT, BigBrotherService.NAME)
                }
                .broadcast()

fun Observable<DeviceInfo>.broadcast() =
        map {
            val port = 4446
            val socket = MulticastSocket(port)
            socket.broadcast = true
            val address = InetAddress.getByName("224.0.0.1")
            socket.connect(address, port)
            val s = createMessage(it.port, it.name)
            val dg = DatagramPacket(s.toByteArray(), s.length)
            socket.send(dg)
            Unit
        }

fun createMessage(port: Int, name: String) =
        "BB:0.1:$port:$name"

data class DeviceInfo(
        val port: Int,
        val name: String
)
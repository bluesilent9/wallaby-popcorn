package org.jellyfin.mobile.setup.ssh

import io.kotest.matchers.shouldBe
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DualStackSocketFactoryTest {
    private val v4 = InetAddress.getByName("192.0.2.1")
    private val v6 = InetAddress.getByName("2001:db8::1")

    private class FakeSocket(private val fail: Boolean) : Socket() {
        var destination: InetSocketAddress? = null
        var closed = false
        override fun connect(endpoint: SocketAddress, timeout: Int) {
            destination = endpoint as InetSocketAddress
            if (fail) throw IOException("unreachable")
        }
        override fun close() { closed = true }
    }

    @Test
    fun `uses IPv6 even when DNS lists IPv4 first`() {
        val socket = FakeSocket(false)
        val factory = DualStackSocketFactory(1000, { arrayOf(v4, v6) }, { socket })
        factory.createSocket("server.example", 22) shouldBe socket
        socket.destination?.address shouldBe v6
        socket.closed shouldBe false
    }

    @Test
    fun `tries the next address and closes the failed socket`() {
        val first = FakeSocket(true)
        val second = FakeSocket(false)
        val sockets = ArrayDeque(listOf(first, second))
        val factory = DualStackSocketFactory(1000, { arrayOf(v4, v6) }, { sockets.removeFirst() })
        factory.createSocket("server.example", 22) shouldBe second
        first.destination?.address shouldBe v6
        first.closed shouldBe true
        second.destination?.address shouldBe v4
    }

    @Test
    fun `closes every failed socket and reports failure`() {
        val first = FakeSocket(true)
        val second = FakeSocket(true)
        val sockets = ArrayDeque(listOf(first, second))
        val factory = DualStackSocketFactory(1000, { arrayOf(v4, v6) }, { sockets.removeFirst() })
        assertThrows(IOException::class.java) { factory.createSocket("server.example", 22) }
        first.closed shouldBe true
        second.closed shouldBe true
    }
}

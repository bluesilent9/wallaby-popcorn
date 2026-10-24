package org.jellyfin.mobile.setup.ssh

import io.kotest.matchers.shouldBe
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SshAddressLookupTest {
    private val v4 = InetAddress.getByName("192.0.2.1")
    private val v6 = InetAddress.getByName("2001:db8::1")

    @Test
    fun `recovers IPv6 when Android returns only IPv4`() {
        resolveSshAddresses("server.example", { arrayOf(v4) }, { listOf(v6) }).toList() shouldBe listOf(v6, v4)
    }

    @Test
    fun `does not repeat a lookup that already returned IPv6`() {
        var extraQueries = 0
        resolveSshAddresses("server.example", { arrayOf(v4, v6) }, { extraQueries++; emptyList() }).toList() shouldBe listOf(v4, v6)
        extraQueries shouldBe 0
    }

    @Test
    fun `keeps IPv4 working if the explicit IPv6 lookup fails`() {
        resolveSshAddresses("server.example", { arrayOf(v4) }, { throw IOException("DNS unavailable") }).toList() shouldBe listOf(v4)
    }

    @Test
    fun `recovers AAAA-only hosts even if the system lookup fails`() {
        resolveSshAddresses("server.example", { throw UnknownHostException() }, { listOf(v6) }).toList() shouldBe listOf(v6)
    }

    @Test
    fun `reports DNS failure when neither lookup succeeds`() {
        assertThrows(UnknownHostException::class.java) {
            resolveSshAddresses("server.example", { throw UnknownHostException() }, { emptyList() })
        }
    }
}

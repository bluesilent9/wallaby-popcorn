package org.jellyfin.mobile.setup

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SshServerAddressTest {
    @Test
    fun `parses username host and default port`() {
        SshServerAddress.parse("ssh://dave-mobile@calder.vip") shouldBe
            SshServerAddress("dave-mobile", "calder.vip", 22)
    }

    @Test
    fun `parses explicit port and IPv6`() {
        val address = SshServerAddress("dave", "2001:db8::1", 2222)
        address.profileAddress() shouldBe "ssh://dave@[2001:db8::1]:2222"
        SshServerAddress.parse(address.profileAddress()) shouldBe address
    }

    @Test
    fun `rejects password path and invalid port`() {
        SshServerAddress.parse("ssh://dave:secret@calder.vip") shouldBe null
        SshServerAddress.parse("ssh://dave@calder.vip/path") shouldBe null
        SshServerAddress.parse("ssh://dave@calder.vip:0") shouldBe null
    }

    @Test
    fun `ordinary Jellyfin urls are not SSH addresses`() {
        SshServerAddress.parse("http://server:8096") shouldBe null
        SshServerAddress.parse("https://server.example.com") shouldBe null
    }
}

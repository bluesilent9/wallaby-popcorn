package org.jellyfin.mobile.setup.ssh

import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AaaaDnsPacketTest {
    private val query = AaaaDnsPacket.query("server.example", 1234)
    private val address = InetAddress.getByName("2001:db8::42")

    private fun response(): ByteArray = ByteArrayOutputStream().also { bytes ->
        val header = query.copyOf()
        header[2] = 0x81.toByte()
        header[3] = 0x80.toByte()
        header[7] = 1
        DataOutputStream(bytes).use {
            it.write(header)
            it.writeShort(0xc00c) // Compressed answer name
            it.writeShort(28)
            it.writeShort(1)
            it.writeInt(60)
            it.writeShort(16)
            it.write(address.address)
        }
    }.toByteArray()

    @Test
    fun `reads a compressed AAAA answer`() {
        AaaaDnsPacket.answers(query, response()) shouldBe listOf(address)
    }

    @Test
    fun `rejects mismatched transaction and question`() {
        val wrongId = response().apply { this[0] = 99 }
        val wrongQuestion = response().apply { this[13] = 'x'.code.toByte() }
        assertThrows(IOException::class.java) { AaaaDnsPacket.answers(query, wrongId) }
        assertThrows(IOException::class.java) { AaaaDnsPacket.answers(query, wrongQuestion) }
    }

    @Test
    fun `rejects truncated and malformed responses`() {
        val truncated = response().apply { this[2] = 0x83.toByte() }
        assertThrows(IOException::class.java) { AaaaDnsPacket.answers(query, truncated) }
        assertThrows(IOException::class.java) { AaaaDnsPacket.answers(query, response().dropLast(1).toByteArray()) }
        assertThrows(IOException::class.java) { AaaaDnsPacket.answers(query, byteArrayOf()) }
    }
}

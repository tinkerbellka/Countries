package dem.alena.countries.data.repository

import java.security.MessageDigest

object ProfilePinHasher {
    fun hash(pin: String): String {
        val normalized = pin.trim()
        if (normalized.isEmpty()) return ""
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    fun matches(pin: String, storedHash: String): Boolean {
        if (storedHash.isEmpty()) return true
        return hash(pin) == storedHash
    }
}

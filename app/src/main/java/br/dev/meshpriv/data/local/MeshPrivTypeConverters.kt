package br.dev.meshpriv.data.local

import androidx.room.TypeConverter
import java.util.Base64

/**
 * Conversores do Room. Usa java.util.Base64 (API 26+) em vez de android.util.Base64
 * para permitir testes em JVM pura.
 *
 * Long? nullable (ex.: receivedAt) é suportado nativamente pelo Room — não precisa de conversor.
 */
class MeshPrivTypeConverters {

    @TypeConverter
    fun byteArrayToBase64(bytes: ByteArray?): String? =
        bytes?.let { Base64.getEncoder().encodeToString(it) }

    @TypeConverter
    fun base64ToByteArray(encoded: String?): ByteArray? =
        encoded?.let { Base64.getDecoder().decode(it) }
}

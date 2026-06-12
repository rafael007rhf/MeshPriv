package br.dev.meshpriv.data.crypto

import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.digests.SHA256Digest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cifra e decifra payloads de mensagens usando ECDH X25519 + HKDF-SHA256 + AES-256-GCM.
 *
 * Formato do payload cifrado: salt(32) + iv(12) + ciphertext+tag(variável)
 *
 * Nunca reutiliza IV — salt e IV são gerados aleatoriamente a cada chamada de encrypt().
 */
class CryptoManager(private val localPrivateKey: ByteArray) {

    companion object {
        private const val SALT_SIZE = 32
        private const val IV_SIZE = 12
        private const val GCM_TAG_BITS = 128
        private const val AES_KEY_SIZE = 32
        private val HKDF_INFO = "meshpriv-v1".toByteArray(Charsets.UTF_8)
    }

    private val secureRandom = SecureRandom()

    fun encrypt(plaintext: String, recipientPublicKey: ByteArray): ByteArray {
        val salt = ByteArray(SALT_SIZE).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { secureRandom.nextBytes(it) }
        val aesKey = deriveAesKey(recipientPublicKey, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertextWithTag = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return salt + iv + ciphertextWithTag
    }

    fun decrypt(payload: ByteArray, senderPublicKey: ByteArray): String {
        require(payload.size > SALT_SIZE + IV_SIZE) { "Payload muito curto" }

        val salt = payload.copyOfRange(0, SALT_SIZE)
        val iv = payload.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
        val ciphertextWithTag = payload.copyOfRange(SALT_SIZE + IV_SIZE, payload.size)
        val aesKey = deriveAesKey(senderPublicKey, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(aesKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertextWithTag), Charsets.UTF_8)
    }

    private fun deriveAesKey(peerPublicKey: ByteArray, salt: ByteArray): ByteArray {
        val agreement = X25519Agreement()
        agreement.init(X25519PrivateKeyParameters(localPrivateKey))
        val sharedSecret = ByteArray(agreement.agreementSize)
        agreement.calculateAgreement(X25519PublicKeyParameters(peerPublicKey), sharedSecret, 0)

        val hkdf = HKDFBytesGenerator(SHA256Digest())
        hkdf.init(HKDFParameters(sharedSecret, salt, HKDF_INFO))
        val aesKey = ByteArray(AES_KEY_SIZE)
        hkdf.generateBytes(aesKey, 0, AES_KEY_SIZE)
        return aesKey
    }
}

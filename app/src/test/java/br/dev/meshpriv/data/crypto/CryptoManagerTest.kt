package br.dev.meshpriv.data.crypto

import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.AEADBadTagException

class CryptoManagerTest {

    private data class KeyPair(val privateKey: ByteArray, val publicKey: ByteArray)

    private fun generateKeyPair(): KeyPair {
        val gen = X25519KeyPairGenerator()
        gen.init(X25519KeyGenerationParameters(SecureRandom()))
        val kp = gen.generateKeyPair()
        return KeyPair(
            privateKey = (kp.private as X25519PrivateKeyParameters).encoded,
            publicKey = (kp.public as X25519PublicKeyParameters).encoded
        )
    }

    @Test
    fun `encrypt then decrypt retorna o texto original`() {
        val alice = generateKeyPair()
        val bob = generateKeyPair()

        val ciphertext = CryptoManager(alice.privateKey).encrypt("Olá, Maria!", bob.publicKey)
        val plaintext = CryptoManager(bob.privateKey).decrypt(ciphertext, alice.publicKey)

        assertEquals("Olá, Maria!", plaintext)
    }

    @Test
    fun `encrypt com texto vazio funciona no roundtrip`() {
        val alice = generateKeyPair()
        val bob = generateKeyPair()

        val ciphertext = CryptoManager(alice.privateKey).encrypt("", bob.publicKey)
        val plaintext = CryptoManager(bob.privateKey).decrypt(ciphertext, alice.publicKey)

        assertEquals("", plaintext)
    }

    @Test
    fun `encrypt com texto longo funciona no roundtrip`() {
        val alice = generateKeyPair()
        val bob = generateKeyPair()
        val texto = "A".repeat(500)

        val ciphertext = CryptoManager(alice.privateKey).encrypt(texto, bob.publicKey)
        val plaintext = CryptoManager(bob.privateKey).decrypt(ciphertext, alice.publicKey)

        assertEquals(texto, plaintext)
    }

    @Test
    fun `duas cifras do mesmo texto produzem ciphertexts diferentes`() {
        val alice = generateKeyPair()
        val bob = generateKeyPair()
        val crypto = CryptoManager(alice.privateKey)

        val cipher1 = crypto.encrypt("mesma mensagem", bob.publicKey)
        val cipher2 = crypto.encrypt("mesma mensagem", bob.publicKey)

        // Salt e IV aleatórios garantem que cada cifragem é única
        assertFalse("Ciphertexts idênticos indicam reutilização de IV", cipher1.contentEquals(cipher2))
    }

    @Test(expected = AEADBadTagException::class)
    fun `decrypt com chave errada lança AEADBadTagException`() {
        val alice = generateKeyPair()
        val bob = generateKeyPair()
        val eve = generateKeyPair()

        // Alice cifra mensagem para Bob
        val ciphertext = CryptoManager(alice.privateKey).encrypt("mensagem secreta", bob.publicKey)

        // Eve tenta decifrar usando sua chave privada — a tag GCM vai falhar
        CryptoManager(eve.privateKey).decrypt(ciphertext, alice.publicKey)
    }
}

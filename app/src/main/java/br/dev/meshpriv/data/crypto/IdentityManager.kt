package br.dev.meshpriv.data.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import br.dev.meshpriv.domain.model.LocalIdentity
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import java.security.MessageDigest
import java.security.SecureRandom

class IdentityManager(private val context: Context) {

    companion object {
        private const val PREFS_SECURE = "identity_secure"
        private const val PREFS_PUBLIC = "identity_public"
        private const val KEY_PRIVATE_KEY = "private_key"
        private const val KEY_PUBLIC_KEY = "public_key"
        private const val KEY_NODE_ID = "node_id"
        private const val KEY_NICKNAME = "nickname"
        private const val NICKNAME_DEFAULT = "Anônimo"
    }

    private val securePrefs: SharedPreferences by lazy { buildEncryptedPrefs() }
    private val publicPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_PUBLIC, Context.MODE_PRIVATE)
    }

    private fun buildEncryptedPrefs(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_SECURE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getIdentity(): LocalIdentity {
        return if (publicPrefs.contains(KEY_NODE_ID)) loadIdentity() else generateAndPersist()
    }

    fun setNickname(nickname: String) {
        publicPrefs.edit().putString(KEY_NICKNAME, nickname).apply()
    }

    private fun generateAndPersist(): LocalIdentity {
        val generator = X25519KeyPairGenerator()
        generator.init(X25519KeyGenerationParameters(SecureRandom()))
        val keyPair = generator.generateKeyPair()

        val privateBytes = (keyPair.private as X25519PrivateKeyParameters).encoded
        val publicBytes = (keyPair.public as X25519PublicKeyParameters).encoded
        val nodeId = deriveNodeId(publicBytes)

        // Chave privada em prefs criptografadas; chave pública e nodeId em prefs comuns
        securePrefs.edit()
            .putString(KEY_PRIVATE_KEY, Base64.encodeToString(privateBytes, Base64.NO_WRAP))
            .apply()
        publicPrefs.edit()
            .putString(KEY_PUBLIC_KEY, Base64.encodeToString(publicBytes, Base64.NO_WRAP))
            .putString(KEY_NODE_ID, nodeId)
            .apply()

        return LocalIdentity(
            nodeId = nodeId,
            nickname = publicPrefs.getString(KEY_NICKNAME, NICKNAME_DEFAULT) ?: NICKNAME_DEFAULT,
            publicKey = publicBytes,
            privateKey = privateBytes
        )
    }

    private fun loadIdentity(): LocalIdentity {
        val privateBytes = Base64.decode(
            checkNotNull(securePrefs.getString(KEY_PRIVATE_KEY, null)) {
                "Chave privada ausente — dados do app podem ter sido corrompidos"
            },
            Base64.NO_WRAP
        )
        val publicBytes = Base64.decode(
            checkNotNull(publicPrefs.getString(KEY_PUBLIC_KEY, null)) { "Chave pública ausente" },
            Base64.NO_WRAP
        )
        val nodeId = checkNotNull(publicPrefs.getString(KEY_NODE_ID, null)) { "nodeId ausente" }
        val nickname = publicPrefs.getString(KEY_NICKNAME, NICKNAME_DEFAULT) ?: NICKNAME_DEFAULT

        return LocalIdentity(
            nodeId = nodeId,
            nickname = nickname,
            publicKey = publicBytes,
            privateKey = privateBytes
        )
    }

    private fun deriveNodeId(publicKey: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(publicKey)
        return digest.take(8).joinToString("") { "%02X".format(it) }
    }
}

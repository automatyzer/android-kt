package com.businessprospector.domain.service

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
        private const val MASTER_KEY_ALIAS = "business_prospector_master_key"
        private const val ENCRYPTED_PREFS_FILE = "encrypted_prefs"
        private const val ENCRYPTION_KEY_PREF = "encryption_key"
    }

    private val encryptionKey: SecretKey by lazy {
        getOrCreateEncryptionKey()
    }

    /**
     * Szyfruje tekst i zwraca zaszyfrowane dane zakodowane w Base64
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val parameterSpec = GCMParameterSpec(TAG_LENGTH, iv)

        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Łączymy IV i zaszyfrowane dane
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Deszyfruje tekst zaszyfrowany metodą encrypt
     */
    fun decrypt(encryptedText: String): String {
        val encryptedData = Base64.decode(encryptedText, Base64.NO_WRAP)

        // Wyodrębniamy IV
        val iv = ByteArray(IV_LENGTH)
        System.arraycopy(encryptedData, 0, iv, 0, iv.size)

        // Wyodrębniamy zaszyfrowane dane
        val encrypted = ByteArray(encryptedData.size - IV_LENGTH)
        System.arraycopy(encryptedData, IV_LENGTH, encrypted, 0, encrypted.size)

        // Deszyfrujemy
        val cipher = Cipher.getInstance(AES_MODE)
        val parameterSpec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec)

        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Bezpiecznie przechowuje i pobiera klucz szyfrowania korzystając z EncryptedSharedPreferences
     */
    private fun getOrCreateEncryptionKey(): SecretKey {
        val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var encodedKey = encryptedPrefs.getString(ENCRYPTION_KEY_PREF, null)

        if (encodedKey == null) {
            // Generujemy nowy klucz AES
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(KEY_SIZE, SecureRandom())
            val key = keyGenerator.generateKey()
            encodedKey = Base64.encodeToString(key.encoded, Base64.NO_WRAP)

            // Zapisujemy klucz
            encryptedPrefs.edit().putString(ENCRYPTION_KEY_PREF, encodedKey).apply()
        }

        val decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, "AES")
    }

    /**
     * Generuje klucz do szyfrowania bazy danych Room
     */
    fun getDatabaseEncryptionKey(): ByteArray {
        val masterKey = MasterKey.Builder(context, "${MASTER_KEY_ALIAS}_db")
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            "${ENCRYPTED_PREFS_FILE}_db",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var encodedKey = encryptedPrefs.getString("db_key", null)

        if (encodedKey == null) {
            // Generujemy losowy 32-bajtowy klucz
            val key = ByteArray(32)
            SecureRandom().nextBytes(key)
            encodedKey = Base64.encodeToString(key, Base64.NO_WRAP)

            // Zapisujemy klucz
            encryptedPrefs.edit().putString("db_key", encodedKey).apply()
        }

        return Base64.decode(encodedKey, Base64.NO_WRAP)
    }
}
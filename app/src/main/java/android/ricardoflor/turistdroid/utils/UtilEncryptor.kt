package android.ricardoflor.turistdroid.utils

import java.lang.Exception
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * Objeto Cifrador
 */
object UtilEncryptor {

    /**
     * Convierte de string a byteArray
     */
    fun encrypt(pwd: String): String? {
        var md: MessageDigest?
        var bytes: ByteArray? = null
        try {
            md = MessageDigest.getInstance("SHA-256")
            bytes = md.digest(pwd.toByteArray(charset("UTF-8")))
        } catch (ex: Exception) {
        }
        return convertToHex(bytes)


    }

    /**
     * Convierte un ByteArray en un String
     */
    fun convertToHex(bytes: ByteArray?): String? {
        val sb = StringBuffer()
        for (i in bytes!!.indices) {
            sb.append(((bytes[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }

}
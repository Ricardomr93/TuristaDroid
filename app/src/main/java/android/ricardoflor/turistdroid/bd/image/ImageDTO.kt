package android.ricardoflor.turistdroid.bd.image

import com.google.gson.annotations.SerializedName

/**
 * Clase modelo de los sitios
 * @property id String
 * @property uri String
 * @property userID String
 * @property siteID String
 */
class ImageDTO (
    @SerializedName("id") val id: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("userID") val userID: String,
    @SerializedName("siteID") val siteID: String,
)
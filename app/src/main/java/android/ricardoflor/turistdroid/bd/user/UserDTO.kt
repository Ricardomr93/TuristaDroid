package android.ricardoflor.turistdroid.bd.user

import com.google.gson.annotations.SerializedName


/**
 * Clase para parsear el JSON
 * @property id String
 * @property name String
 * @property nameUser String
 * @property password String
 * @property email String
 * @property image String
 * @property twitter String
 * @property instagram String
 * @property facebook String
 * @constructor
 */
class UserDTO (
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("nameUser") val nameUser: String,
        @SerializedName("password") val password: String,
        @SerializedName("email") val email: String,
        @SerializedName("image") val image: String,
        @SerializedName("twitter") val twitter: String,
        @SerializedName("instagram") val instagram: String,
        @SerializedName("facebook") val facebook: String,
    )
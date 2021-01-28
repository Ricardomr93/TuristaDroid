package android.ricardoflor.turistdroid.bd.session

import com.google.gson.annotations.SerializedName
import io.realm.annotations.Required

/**
 * Clase para parsear el JSON
 * @property id String
 * @property time String
 * @property token String
 * @constructor
 */
class SessionDTO(
    @SerializedName("id") val id: String,
    @SerializedName("time") val time: String,
    @SerializedName("token") val token: String,
    )

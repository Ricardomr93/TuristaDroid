package android.ricardoflor.turistdroid.bd.site

import com.google.gson.annotations.SerializedName

/**
 * Clase modelo de los sitios
 * @property id String
 * @property name String
 * @property site String
 * @property date String
 * @property rating Double
 * @property latitude Double
 * @property longitude Double
 */
class SiteDTO (
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("site") val site: String,
    @SerializedName("date") val date: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("userID") val userID : String,
    @SerializedName("votos") val votos : Int,
    @SerializedName("imageID") val imageID : String

)
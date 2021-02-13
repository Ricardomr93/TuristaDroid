package android.ricardoflor.turistdroid.bd.site

import android.ricardoflor.turistdroid.bd.image.Image
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Clase modelo de los sitios
 * @property id String
 * @property name String
 * @property site String
 * @property date String
 * @property rating Double
 * @property latitude Double
 * @property longitude Double
 * @property userID String
 * @property votos ArrayList<String>
 * @property images ArrayList<String>
 */
open class Site(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var site: String = "",
    var date: String = "",
    var rating: Double = 0.0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var userID: String = "",
    var votos: ArrayList<String> = ArrayList(),
    var images: ArrayList<String> = ArrayList(),
) : Serializable {

    /**
     * Constructor
     * @property name String
     * @property site String
     * @property date String
     * @property rating Double
     * @property latitude Double
     * @property longitude Double
     * @property userID String
     * @property votos ArrayList<String>
     * @property images ArrayList<String>
     * @constructor
     */
    constructor(
        name: String,
        site: String,
        date: String,
        rating: Double,
        latitude: Double,
        longitude: Double,
        userID: String,
        votos: ArrayList<String>,
        images: ArrayList<String>,
    ) :
            this((UUID.randomUUID().toString()), name, site, date, rating, latitude, longitude, userID, votos, images)

    /**
     * Metodo sobrescrito para escribir en pantalla un Site
     */
    override fun toString(): String {
        return "Site(id=$id, name='$name', site='$site', date=$date, rating=$rating, latitude=$latitude, longitude=$longitude, userID=$userID)"
    }

}

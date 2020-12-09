package android.ricardoflor.turistdroid.bd.site

import android.ricardoflor.turistdroid.bd.image.Image
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Clase modelo de los sitios
 * @property id Long
 * @property name String
 * @property image String
 * @property site String
 * @property date Date
 * @property rating Int
 * @property latitude Int
 * @property longitude Int
 */
open class Site(
    @PrimaryKey
    var id: Long = 0,
    var name: String = "",
    var image: RealmList<Image> = RealmList(),
    var site: String = "",
    var date: Date = Date(),
    var rating: Int = 0,
    var latitude: Int = 0,
    var longitude: Int = 0
) : RealmObject(){
    override fun toString(): String {
        return "Site(id=$id, name='$name', image='$image', site='$site', date=$date, rating=$rating, latitude=$latitude, longitude=$longitude)"
    }
    constructor(name: String,image: RealmList<Image>,site: String,date: Date,rating: Int,latitude: Int,longitude: Int)
            :this(System.currentTimeMillis()/1000,name, image, site, date, rating, latitude, longitude)
}

package android.ricardoflor.turistdroid.bd

import io.realm.RealmObject
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
    var id: Long = 0,
    var name: String = "",
    var image: String = "",
    var site: String = "",
    var date: Date = Date(),
    var rating: Int = 0,
    var latitude: Int = 0,
    var longitude: Int = 0
) : RealmObject()

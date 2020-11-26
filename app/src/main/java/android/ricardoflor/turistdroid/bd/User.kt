package android.ricardoflor.turistdroid.bd

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

/**
 * Clase modelo del usuario
 * @property id long
 * @property name String
 * @property nameUser String
 * @property email String
 * @property image String
 * @property twitter String
 * @property instagram String
 * @property facebook String
 * @property places RealmList<Site>
 */
open class User(

    var id: Long = 0,
    var name: String = "",
    var nameUser: String = "",
    @PrimaryKey
    var email: String = "",
    var password: String = "",
    var image: String = "",
    var twitter: String = "",
    var instagram: String = "",
    var facebook: String = "",
    var places: RealmList<Site> = RealmList()// relacion MxM
) : RealmObject() {
    override fun toString(): String {
        return "Usuario(id=$id, name='$name', nameUser='$nameUser', password='$password', iamage='$image', email='$email', twitter='$twitter', instagram='$instagram', facebook='$facebook')"
    }
}



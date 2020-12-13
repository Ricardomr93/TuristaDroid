package android.ricardoflor.turistdroid.bd.user

import android.ricardoflor.turistdroid.bd.site.Site
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

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
    /**
     * Metodo sobrescrito para escribir en pantalla un User
     */
    override fun toString(): String {

        return "Usuario(name='$name', nameUser='$nameUser',email='$email', password='$password', twitter='$twitter', instagram='$instagram', facebook='$facebook', places$places, image='$image',\n)"
    }
}



package android.ricardoflor.turistdroid.bd.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.io.Serializable
import java.util.*

/**
 * Clase modelo del usuario
 * @property id String
 * @property name String
 * @property nameUser String
 * @property password String
 * @property email String
 * @property image String
 * @property twitter String
 * @property instagram String
 * @property facebook String
 */
@RealmClass
open class User(
    @PrimaryKey
    var id: String = "",
    @Required
    var name: String = "",
    @Required
    var nameUser: String = "",
    @Required
    var password: String = "",
    @Required
    var email: String = "",
    var image: String = "",
    var twitter: String = "",
    var instagram: String = "",
    var facebook: String = "",
    ) : RealmObject(), Serializable {

        /**
         * Constructor
         * @param name String
         * @param nameUser String
         * @param password String
         * @param email String
         * @param image String
         * @param twitter String
         * @param instagram String
         * @param facebook String
         * @constructor
         */
        constructor(
            name: String,
            nameUser: String,
            password: String,
            email: String,
            image: String,
            twitter: String,
            instagram: String,
            facebook: String,
        ) :
        this((UUID.randomUUID().toString()), name, nameUser, password, email, image, twitter, instagram, facebook)

        /**
         * Metodo sobrescrito para escribir en pantalla un User
         */
        override fun toString(): String {
            return "Usuario(id=$id, name='$name', nameUser='$nameUser', password='$password', email='$email', image='$image', twitter='$twitter', instagram='$instagram', facebook='$facebook')"
        }
}



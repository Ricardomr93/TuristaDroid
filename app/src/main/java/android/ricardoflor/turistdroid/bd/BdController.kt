package android.ricardoflor.turistdroid.bd

import io.realm.Realm.*
import io.realm.kotlin.where;
import android.content.Context;
import io.realm.RealmConfiguration


/**
 * Controlador para los usuarios
 */
object BdController {
    private const val DATOS_BD = "turista_droid_bd"
    private const val DATOS_BD_VERSION = 1L

    /**
     * funcion que inicializa Realm para la migracion en caso de cambiar el controlador
     * @param context Context
     */
    fun initRealm(context: Context?) {
        init(context)
        val config = RealmConfiguration.Builder()
            .name(DATOS_BD)//nombre del schema
            .schemaVersion(DATOS_BD_VERSION)//version del schema
            .deleteRealmIfMigrationNeeded()//borra los datos si necesita migracion
            .build()
        setDefaultConfiguration(config)
    }

    //User**********
    /**
     * Insert Usuario
     * @param user User
     */
    fun insertUser(user: User) {
        getDefaultInstance().executeTransaction {
            it.copyToRealm(user)
        }
    }

    /**
     * Delete Usuario
     * @param user User
     */
    fun deleteUser(user: User) {
        getDefaultInstance().executeTransaction() {
            it.where<User>().equalTo("email", user.email).findFirst()?.deleteFromRealm()
        }
    }

    fun selectAllUser():MutableList<User>?{
        return getDefaultInstance().copyFromRealm(
            getDefaultInstance().where<User>().findAll()
        )
    }

    /**
     * Hace una select por  email
     * @param email
     */
    fun selectByEmail(email: String): User? {
        return getDefaultInstance().copyFromRealm(
            getDefaultInstance().where<User>().equalTo("email", email).findFirst()
        )
    }

    /**
     * Update de User
     * @param user User
     */
    fun updateUser(user: User) {
        getDefaultInstance().executeTransaction {
            it.copyToRealmOrUpdate(user)
        }
    }

    /**
     * Elimina todos
     */
    fun removeAll() {
        getDefaultInstance().executeTransaction {
            it.deleteAll();
        }
    }

    //Site***********
    /**
     * Insert Site
     * @param site Site
     */
    fun insertSite(site: Site) {
        getDefaultInstance().executeTransaction {
            it.copyToRealm(site)
        }
    }

    /**
     * Delete Site
     * @param site Site
     */
    fun deleteSite(site: Site) {
        getDefaultInstance().executeTransaction() {
            it.where<Site>().equalTo("id", site.id).findFirst()?.deleteFromRealm()
        }
    }

    /**
     * Select de todos los sitios
     */
    fun selectAllSite():MutableList<Site>?{
        return getDefaultInstance().copyFromRealm(
            getDefaultInstance().where<Site>().findAll()
        )
    }

    /**
     * Ordena segun el Rating
     */
    fun orderByRating() {
        getDefaultInstance().executeTransaction {
            it.where<Site>().sort("rating").findAll()
        }
    }

    /**
     * Ordena segun la fecha
     */
    fun orderByDate() {
        getDefaultInstance().executeTransaction {
            it.where<Site>().sort("Date").findAll()
        }
    }

    /**
     * Ordena segun nombre
     */
    fun orderByName() {
        getDefaultInstance().executeTransaction {
            it.where<Site>().sort("name").findAll()
        }
    }
    //fun selectById
}
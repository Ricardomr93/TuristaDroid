@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package android.ricardoflor.turistdroid.bd

import io.realm.Realm.*
import io.realm.kotlin.where;
import android.content.Context;
import android.util.Log
import io.realm.Realm
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

    //CONTROLES DE LA APP
    /**
     * Elimina todos
     */
    fun removeAll() {
        getDefaultInstance().executeTransaction {
            it.deleteAll();
            Log.i("util","BD borrada")
        }
    }

    /**
     * Cierra la base de datos
     */
    fun close() {
        Realm.getDefaultInstance().close()
    }
}
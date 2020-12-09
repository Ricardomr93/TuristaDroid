package android.ricardoflor.turistdroid.bd.site

import io.realm.Realm
import io.realm.kotlin.where

object SiteController {
    //CONTROLES PARA SITE
    /**
     * Insert Site
     * @param site Site
     */
    fun insertSite(site: Site) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(site)
        }
    }

    /**
     * Delete Site
     * @param site Site
     */
    fun deleteSite(site: Site) {
        Realm.getDefaultInstance().executeTransaction() {
            it.where<Site>().equalTo("id", site.id).findFirst()?.deleteFromRealm()
        }
    }

    /**
     * Select de todos los sitios
     */
    fun selectAllSite():MutableList<Site>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Site>().findAll()
        )
    }

    /**
     * Ordena segun el Rating
     */
    fun orderByRating() {
        Realm.getDefaultInstance().executeTransaction {
            it.where<Site>().sort("rating").findAll()
        }
    }

    /**
     * Ordena segun la fecha
     */
    fun orderByDate() {
        Realm.getDefaultInstance().executeTransaction {
            it.where<Site>().sort("Date").findAll()
        }
    }

    /**
     * Ordena segun nombre
     */
    fun orderByName() {
        Realm.getDefaultInstance().executeTransaction {
            it.where<Site>().sort("name").findAll()
        }
    }
}
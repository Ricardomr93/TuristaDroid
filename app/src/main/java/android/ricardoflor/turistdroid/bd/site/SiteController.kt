package android.ricardoflor.turistdroid.bd.site

import com.google.android.gms.maps.model.LatLng
import android.ricardoflor.turistdroid.bd.user.User
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
     * Borra todos los sitios
     */
    fun deleteAllSite(){
        Realm.getDefaultInstance().executeTransaction {
            it.where<Site>().findAll().deleteAllFromRealm()
        }
    }

    /**
     * Update Site
     * @param site Site
     */
    fun updateSite(site: Site) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealmOrUpdate(site)
        }
    }

    /**
     * Busca todos los sitios que esten entre los parametros dados
     * @param site : Site
     * @param distance : Double
     */
    fun selectByNear(latitude:Double,longitude:Double,distance:Double):MutableList<Site>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Site>().between("longitude",(longitude-distance),
                (longitude+distance)).and()
                .between("latitude",(latitude-distance),(latitude+distance)).findAll()
        )
    }

    /**
     * Select de todos los sitios
     */
    fun selectAllSite():MutableList<Site>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Site>().findAll()
        )
    }
}
package android.ricardoflor.turistdroid.bd.image

import io.realm.Realm
import io.realm.kotlin.where

object ImageController {

    /**
     * Select de la primera imagen que encuentre
     */
    fun selectFirstImage(image: Image): Image? {
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Image>().equalTo("id", image.id).findFirst()
        )
    }

    /**
     * Inserta una imagen
     */
    fun insertImage(image: Image) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(image)
        }
    }
    fun deleteAllImages(){
        Realm.getDefaultInstance().executeTransaction {
            it.where<Image>().findAll().deleteAllFromRealm()
        }
    }

    /**
     * Select de todas las imagenes
     */
    fun selectAllImage(): MutableList<Image> {
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Image>().findAll()
        )
    }
}
package android.ricardoflor.turistdroid.bd.image

import io.realm.Realm
import io.realm.kotlin.where

object ImageController {

    /**
     * Select session
     */
    fun selectFirstImage(image: Image): Image? {
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Image>().equalTo("id",image.id).findFirst()
        )
    }
}
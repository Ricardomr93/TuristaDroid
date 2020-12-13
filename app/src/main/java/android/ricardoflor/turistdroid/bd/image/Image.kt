package android.ricardoflor.turistdroid.bd.image

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Image (
    @PrimaryKey
    var id: Long = 0,
    var image : String = ""
): RealmObject(){
    constructor(image:String):this(System.currentTimeMillis()/1000,image)
}
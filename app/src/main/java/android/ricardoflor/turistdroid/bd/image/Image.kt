package android.ricardoflor.turistdroid.bd.image

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.io.Serializable
import java.util.*

open class Image (
    @PrimaryKey
    var id: String = "",
    @Required
    var uri: String = "",
    @Required
    var userID: String = "",
    @Required
    var siteID: String = "",
): RealmObject(), Serializable {
    constructor(uri: String, userID: String, siteID: String) :
    this((UUID.randomUUID().toString()), uri, userID, siteID)
}
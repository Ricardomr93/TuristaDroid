package android.ricardoflor.turistdroid.bd.session

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Session(
    @PrimaryKey
    var useremail: String = ""
) : RealmObject()

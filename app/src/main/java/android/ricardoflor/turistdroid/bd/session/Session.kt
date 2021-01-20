package android.ricardoflor.turistdroid.bd.session

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.io.Serializable

open class Session(
    @PrimaryKey
    var userId: String = "",
    @Required
    var time: String = "",
    @Required
    var token: String = "",
) : RealmObject(), Serializable {
    fun changeSession(session: Session) {
        this.userId = session.userId
        this.time = session.time
        this.token = session.token
    }
}

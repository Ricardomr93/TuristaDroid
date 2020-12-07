package android.ricardoflor.turistdroid.bd.session

import android.util.Log
import io.realm.Realm
import io.realm.kotlin.where

object SessionController {
    //CONTROLES DE LA SESSION
    /**
     * Crea una sesion con el email del user
     */
    fun insertSession(session: Session){
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(session)
            Log.i("util","Session creada")
        }
    }

    /**
     * Cierra la sesion y borra el email del user
     */
    fun deleteSession(session : Session){
        Log.i("util","session a borrar: "+session.useremail)
        Realm.getDefaultInstance().executeTransaction {
            it.where<Session>().equalTo("useremail", session.useremail).findFirst()?.deleteFromRealm()
            Log.i("util","Session borrada")
        }
    }

    /**
     * Select session
     */
    fun selectSession(): Session? {
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Session>().findFirst()
        )
    }
}
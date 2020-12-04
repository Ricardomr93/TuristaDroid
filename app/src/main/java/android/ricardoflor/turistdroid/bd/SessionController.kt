package android.ricardoflor.turistdroid.bd

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
        }
    }

    /**
     * Cierra la sesion y borra el email del user
     */
    fun deleteSession(session : Session){
        Realm.getDefaultInstance().executeTransaction {
            it.where<Session>().equalTo("useremail", session.useremail).findFirst()?.deleteFromRealm()
        }
    }
}
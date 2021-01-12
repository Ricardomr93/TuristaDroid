package android.ricardoflor.turistdroid.bd.user

import io.realm.Realm
import io.realm.kotlin.where

object UserController {

    //CONTROLES PARA USER
    /**
     * Insert Usuario
     * @param user User
     */
    fun insertUser(user: User) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(user)
        }
    }

    /**
     * Borra todos los usuarios
     */
    fun deleteAllUsers() {
        Realm.getDefaultInstance().executeTransaction {
            it.where<User>().findAll().deleteAllFromRealm()
        }
    }

    /**
     * Delete Usuario
     * @param user User
     */
    fun deleteUser(email: String) {
        Realm.getDefaultInstance().executeTransaction() {
            it.where<User>().equalTo("email", email).findFirst()?.deleteFromRealm()
        }
    }

    /**
     * Selecciona todos los usuarios
     */
    fun selectAllUser(): MutableList<User>? {
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().findAll()
        )
    }

    /**
     * Select por  email
     * @param email
     */
    fun selectByEmail(email: String): User? {
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<User>().equalTo("email", email).findFirst()
        )
    }

    /**
     * Update de User
     * @param user User
     */
    fun updateUser(user: User) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealmOrUpdate(user)
        }
    }
}
package android.ricardoflor.turistdroid.utils

import android.ricardoflor.turistdroid.bd.session.Session
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.util.Log

object UtilSession {

    private var SESSION = Session();

    /**
     * Comrpueba que existe una sesión abierta
     * @param context Context
     * @return Boolean
     */
    fun comprobarSesion(): Boolean {
        // Abrimos las preferencias en modo lectura
        try {
            SESSION = SessionController.selectSession()!!
            Log.i("util", "Usuario EMAIL: " + SESSION.useremail)
        } catch (ex: IllegalArgumentException) {
            Log.i("util", "Usuario EMAIL: " + SESSION.useremail)
        }
        return SESSION.useremail.isNotEmpty()
    }
        /**
         * Crea la session con el email del usuario  que se acaba de logear
         */
        fun createSession(email: String) {
            SESSION = Session(email)
            SessionController.insertSession(SESSION)
            Log.i("util", "session: " + SESSION.useremail + " creada.")
        }

        /**
         * Borra la session del email y lo pone a vacio
         */
        fun deleteSession() {
            try {
                SESSION = SessionController.selectSession()!!
                Log.i("util", "SESSION "+SESSION.useremail)
                SessionController.deleteSession(SESSION)
            }catch (ex: IllegalArgumentException){
            }

        }

    }
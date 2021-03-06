package android.ricardoflor.turistdroid.utils

import android.content.Context
import android.content.Intent
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.session.Session
import android.ricardoflor.turistdroid.bd.session.SessionDTO
import android.ricardoflor.turistdroid.bd.session.SessionMapper
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserDTO
import android.ricardoflor.turistdroid.bd.user.UserMapper
import android.util.Log
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

object UtilSession {

    private var MAX_LIMITE_DAY = 1

    /**
     * Comprueba si hay una sesion abierta
     * @param context Context
     * @return si existe o no una sesion
     */
    fun sessionExist(context: Context): Boolean {
        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val session = prefs.getString("sessionUID", "").toString()
        Log.i("rest", "Usuario ID: $session")
        return session.isNotEmpty()
    }

    /**
     * Metodo que comprueba que el id de
     */
    fun comprobarIDSession(id: String, context: Context) {
        val turistREST = TuristAPI.service
        Log.i("Rest", "id a comprobar: $id")
        val call = turistREST.sesionGetById(id)
        call.enqueue((object : Callback<SessionDTO> {
            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {
                Log.i("REST", "onResponse comprobarIDSession")
                Log.i("REST", "isSuccessful comprobarIDSession")
                if (response.isSuccessful) {
                    Log.i("Rest", "existe sesion en bd")
                    val session = SessionMapper.fromDTO(response.body()!!)//si saca crea session preferencias
                    createSessionPref(context, session)
                } else {//si no devuelve ninguna fila crea una session
                    Log.i("Rest", "No existe sesion en bd")
                    createSession(id, context)
                }
            }
            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                Log.i("REST", "salta error comprobarIDSession")
                Toast.makeText(
                    context,
                    context.getText(R.string.service_error).toString() + t.localizedMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }))
    }

    /**
     * Metodo que crea una sesion en las preferencias
     */
    fun createSessionPref(context: Context, session: Session) {
        val preferences = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("sessionUID", session.id)
        editor.putString("sessionTime", session.time)
        editor.putString("sessionToken", session.token)
        editor.apply()
        Log.i("rest", "creado sesion: ${session.id}")
    }

    /**
     * Metodo que borra la sesion en las preferencias
     */
    fun deleteSessionPref(context: Context) {
        Log.i("Rest", "session preferencias borrada")
        val preferences = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove("sessionUID")
        editor.remove("sessionTime")
        editor.remove("sessionToken")
        editor.apply()
    }

    /**
     * metodo que comprueba si ha expirado o no el tiempo
     */
    fun timeExpired(context: Context): Boolean {
        var expired = true
        val preferences = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        val time = preferences.getString("sessionTime", "").toString()
        Log.i("rest", "Tiempo sesion: $time")
        Log.i("rest", "Tiempo ahora: ${Instant.now()}")
        val timeSession = Instant.parse(time)
        val days = ChronoUnit.DAYS.between(timeSession, Instant.now())
        if (days <= MAX_LIMITE_DAY) {
            expired = false
        }
        return expired
    }


    /**
     * Crea la session con el id del usuario
     */
    fun createSession(id: String, context: Context) {
        val i = Instant.now()
        val sess = Session(
            id = id,
            time = Instant.now().toString(),
            token = UUID.randomUUID().toString()
        )

        val turistREST = TuristAPI.service
        val call: Call<SessionDTO> = turistREST.sesionPost(SessionMapper.toDTO(sess))
        call.enqueue(object : Callback<SessionDTO> {
            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {
                if (response.isSuccessful) {
                    Log.i("Rest", "Session: $sess.")
                    createSessionPref(context, sess)
                }
            }

            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                Toast.makeText(
                    context,
                    context.getText(R.string.service_error).toString() + t.localizedMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        })
    }

    /**
     * Borra la session del email y lo pone a vacio
     */
    fun closeSession(context: Context) {
        val turistREST = TuristAPI.service
        val call: Call<SessionDTO> = turistREST.sesionDelete(USER.id)
        call.enqueue((object : Callback<SessionDTO> {

            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {
                Log.i("REST", "sesionDelete onResponse")
                if (response.isSuccessful) {
                    Log.i("REST", "sesionDelete isSuccessful")
                    Log.i("REST", "sesionDelete ok")
                } else {
                    Log.i("REST", "Error: SesionDelete isSuccessful")
                }
            }

            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                Log.i("REST", "sesionDelete failure")
            }
        }))

    }

    /**
     * Metodo que recoge el id del usuario de la session activa
     */
    fun getUserID(context: Context): String {
        val prefs = context.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        return prefs.getString("sessionUID", "").toString()
    }


}
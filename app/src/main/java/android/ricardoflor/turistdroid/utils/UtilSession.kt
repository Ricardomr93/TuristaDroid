package android.ricardoflor.turistdroid.utils

import android.content.Context
import android.ricardoflor.turistdroid.MyApplication.Companion.SESSION
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.apirest.TuristREST
import android.ricardoflor.turistdroid.bd.session.Session
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.session.SessionDTO
import android.ricardoflor.turistdroid.bd.session.SessionMapper
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.TypedArrayUtils.getText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.util.*

object UtilSession {
    /**
     * Comrpueba que existe una sesión abierta
     * @param context Context
     * @return Boolean
     */
    fun comprobarSesion(context: Context): Boolean {
        var haveSession = false
        val turistREST = TuristAPI.service
        val call: Call<SessionDTO> = turistREST.sesionGetById(USER.id)
        call.enqueue(object : Callback<SessionDTO> {
            override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {
                haveSession = true
            }
            override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                Toast.makeText(context,
                    context.getText(R.string.service_error).toString() + t.localizedMessage,
                    Toast.LENGTH_LONG)
                    .show()
            }
        })
        return haveSession

    }
        /**
         * Crea la session con el email del usuario  que se acaba de logear
         */
        fun createSession(id: String,context: Context) {

             val sess = Session(
                userId = id,
                time = Instant.now().toString(),
                token = UUID.randomUUID().toString()
            )

            val turistREST = TuristAPI.service
            val call: Call<SessionDTO> = turistREST.sesionPost(SessionMapper.toDTO(sess))
            call.enqueue(object : Callback<SessionDTO> {
                override fun onResponse(call: Call<SessionDTO>, response: Response<SessionDTO>) {
                    if (response.isSuccessful){
                       SESSION = sess
                    }
                }
                override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                    Toast.makeText(context,
                        context.getText(R.string.service_error).toString() + t.localizedMessage,
                        Toast.LENGTH_LONG)
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
                    if (response.isSuccessful) {
                        Log.i("REST", "sesionDelete ok")
                    } else {
                        Log.i("REST", "Error: SesionDelete isSuccessful")
                    }
                }

                override fun onFailure(call: Call<SessionDTO>, t: Throwable) {
                    Toast.makeText(context,
                        context.getText(R.string.service_error).toString() + t.localizedMessage,
                        Toast.LENGTH_LONG)
                        .show()
                }
            }))

        }
    }
package android.ricardoflor.turistdroid.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.ricardoflor.turistdroid.R
import android.util.Log
import com.google.android.material.snackbar.Snackbar

object UtilNet {

    /**
     * Método que comprueba si está conectado a internet y devuelve el resultado
     * @param context Context?
     * @return Boolean
     */
    fun hasInternetConnection(context: Context?): Boolean {
        var conne = false
        if (context != null) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            Log.i("Net", "Conexion : Cellular")
                            conne = true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            Log.i("Net", "Conexion : WIFI")
                            conne = true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            Log.i("Net", "Conexion : ETHERNET")
                            conne = true
                        }
                    }
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    Log.i("Net", "ActiveNetworkInfo.isConnected")
                    conne = true
                }
            }
        } else {
            Log.i("Net", "Sin red")
        }
        return conne
    }

    /**
     * Metodo que comprueba el tipo de conexion y si hay alguna activa
     */
    private fun typeConnect(capabilities: NetworkCapabilities): Boolean{
        var conne = false
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Net", "Conexion : Cellular")
                    conne = true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Net", "Conexion : WIFI")
                    conne = true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Net", "Conexion : ETHERNET")
                    conne = true
                }
            }
        }
        return conne
    }
}
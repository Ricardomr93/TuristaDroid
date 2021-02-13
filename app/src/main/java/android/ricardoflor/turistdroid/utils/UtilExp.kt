package android.ricardoflor.turistdroid.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.image.ImageDTO
import android.ricardoflor.turistdroid.bd.image.ImageMapper
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteDTO
import android.ricardoflor.turistdroid.bd.site.SiteMapper
import android.ricardoflor.turistdroid.export.Export
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object UtilExp {

    private lateinit var SITES: MutableList<Site>
    private lateinit var IMAGES: MutableList<Image>
    private var clientREST = TuristAPI.service


    /**
     * Cuadro de dialogo para la confirmacion de la exportacion de archivos
     */
    fun export(context: Context) {
        AlertDialog.Builder(context)
            .setIcon(R.drawable.ic_bandeja_de_salida)
            .setTitle(context.getText(R.string.caution))
            .setMessage(context.getText(R.string.messExport))
            .setPositiveButton(context.getString(R.string.ok)) { dialog, which -> exportFiles(context!!) }//TODO -> no inserta bien porque no está inicializado
            .setNegativeButton(context.getString(R.string.Cancel), null)
            .show()
    }

    /**
     * Exporta los datos de toda la BD menos SESSION
     * @param context Context
     * @return Boolean
     */
    fun exportFiles(context: Context){
        //coge todos los datos de todos los modelos
        imagesRest(context)
    }

    /**
     * Metodo para recuperar los sitios segun id User
     */
    private fun imagesRest(context: Context) {
        val call: Call<List<ImageDTO>> = clientREST.imageGetByIDUser(USER.id)
        call.enqueue((object : Callback<List<ImageDTO>> {

            override fun onResponse(call: Call<List<ImageDTO>>, response: Response<List<ImageDTO>>) {
                if (response.isSuccessful) {
                    Log.i("REST", "onResponse imagesRest")
                    IMAGES = (ImageMapper.fromDTO(response.body() as MutableList<ImageDTO>)) as MutableList<Image>
                    sitesRest(context)
                }
            }

            override fun onFailure(call: Call<List<ImageDTO>>, t: Throwable) {
                Log.i("REST", "onFailure imagesRest")
                Toast.makeText(
                    context,
                    R.string.service_error,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }))
    }

    /**
     * Metodo para recuperar los sitios segun id User
     */
    private fun sitesRest(context: Context) {
/*
        val call: Call<List<SiteDTO>> = clientREST.siteGetByUserID(USER.id)
        call.enqueue((object : Callback<List<SiteDTO>> {

            override fun onResponse(call: Call<List<SiteDTO>>, response: Response<List<SiteDTO>>) {
                if (response.isSuccessful) {
                    Log.i("REST", "onResponse sitesRest")
                    SITES = (SiteMapper.fromDTO(response.body() as MutableList<SiteDTO>)) as MutableList<Site>
                    val impExp = Export(
                        sites = SITES,
                        images = IMAGES
                    )
                    val ExpGson = Gson().toJson(impExp)
                    // Archivo el objeto JSON
                    fileExport(context, ExpGson.toString())

                    Log.i("REST", SITES.size.toString())
                    Log.i("REST", IMAGES.size.toString())
                }
            }

            override fun onFailure(call: Call<List<SiteDTO>>, t: Throwable) {
                Log.i("REST", "onFailure sitesRest")
                Toast.makeText(
                    context,
                    R.string.service_error,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }))
*/    }

    /**
     * Archiva los datos
     * @param context Context
     * @param datos String
     * @return Boolean
     */
    fun fileExport(context: Context, datos: String) {
        //guarda en la memoria interna del movil
        val dirImpExp =
            File((context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath) + "/TuristDroid")
        if (!dirImpExp.exists())
            dirImpExp.mkdir()
        val file = File(dirImpExp, "turistDroid.json")
        Log.i("import", file.toString())
        try {
            val strToBytes: ByteArray = datos.toByteArray()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Files.write(Paths.get(file.toURI()), strToBytes)
                Toast.makeText(context!!, context.getText(R.string.exportok), Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            Log.i("impor", "Error: " + ex.localizedMessage)
        }
    }
}
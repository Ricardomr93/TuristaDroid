package android.ricardoflor.turistdroid.activities.ui.mySites

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.widget.*
import androidx.fragment.app.FragmentManager
import java.util.*

class SiteFragment(modo: Boolean) : Fragment() {

    private val modo = modo
    private lateinit var root: View

    // Variables Site
    private var lugar: Site? = null
    private var name: String? = null
    private var image: Bitmap? = null
    private var site: String? = null
    private var date: Date? = null
    private var rating: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // Variables de camara
    private val GALERIA = 1
    private val CAMARA = 2
    private lateinit var IMAGEN_URI: Uri
    private val IMAGEN_DIRECTORY = "/MisLugares"
    private val IMAGEN_PROPORCION = 600
    private lateinit var FOTO: Bitmap
    private var IMAGEN_COMPRESION = 60
    private val IMAGEN_PREFIJO = "lugar"
    private val IMAGEN_EXTENSION = ".jpg"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_site, container, false)
        editCreateMode()
        val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
        cajafecha.setOnClickListener{
            showDatePickerDialog()
        }


        return root
    }

    /**
     * Funcion que muestra un DatePicker
     */
    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(this.parentFragmentManager, "datePicker")
    }

    fun onDateSelected(day:Int, month:Int, year: Int){
        val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
        cajafecha.setText("$day/$month/$year")
    }

    /**
     * Metodo para abrir el fragment en edicion o en creacion
     */
    fun editCreateMode() {
        val btn: Button = root.findViewById(R.id.buttonSiteAddUpdate)

        if (modo) {
            // Fragment de Creacion
            btn.text = getString(R.string.add)

            add(btn)
        }

        // Fragment de Edicion
        else {
            btn.text = getString(R.string.update)

            update(btn)
        }

    }

    /**
     * Metodo para aniadir un sitio
     */
    fun add(btn: Button) {

       // lugar.name = root.findViewById(R.id.txtNameSiteSite)
        val txtSiteName: EditText = root.findViewById(R.id.txtNameSiteSite)
        val txtLocalizacion: EditText = root.findViewById(R.id.txtSiteSite)
//        val txtDate: EditText = root.findViewById(R.id.txtDateSite)
//        val rating: RatingBar = root.findViewById(R.id.ratingBar)
//        image

        btn.setOnClickListener {
            Toast.makeText(context!!, "AÑADO", Toast.LENGTH_SHORT).show()

            //SiteController.insertSite(lugar)

        }
    }

    /**
     * Metodo para editar un sitio
     */
    private fun update(btn: Button) {
        val txtSiteName: EditText = root.findViewById(R.id.txtNameSiteSite)
        val txtLocalizacion: EditText = root.findViewById(R.id.txtSiteSite)
//        val txtDate: EditText = root.findViewById(R.id.txtDateSite)
//        val rating: RatingBar = root.findViewById(R.id.ratingBar)
//        image

        btn.setOnClickListener {
            Toast.makeText(context!!, "EDITO", Toast.LENGTH_SHORT).show()


            //SiteController.insertSite(lugar)

        }
    }
}
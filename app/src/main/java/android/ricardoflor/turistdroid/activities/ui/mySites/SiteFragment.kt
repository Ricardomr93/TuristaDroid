package android.ricardoflor.turistdroid.activities.ui.mySites

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.util.Log
import android.widget.*
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.realm.RealmList
import java.io.IOException

class SiteFragment(modo: Int, site: Site?) : Fragment() {

    private val modo = modo
    private val sitio: Site? = site
    private lateinit var root: View

    // Variables Site
    private lateinit var lugar: Site
    private var name: String? = null
    private var image: RealmList<Image> = RealmList()
    private var site: String? = null
    private var date: String? = null
    private var rating: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // Variables de camara
    private val GALLERY = 1
    private val CAMERA = 2
    private lateinit var IMAGEN: Uri
    private var modoFoto: Int = 0

    // Variables Slider Images
    private var images: Array<Bitmap> = arrayOf()
    private lateinit var adapter: PagerAdapter

    // Vibrador
    private var vibrator: Vibrator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_site, container, false)
        initEditCreateMode()
        initPermisos()

        val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
        cajafecha.setOnClickListener {
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

    fun onDateSelected(day: Int, month: Int, year: Int) {
        val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
        cajafecha.setText("$day/$month/$year")
    }

    /**
     * Metodo para abrir el fragment en edicion o en creacion
     */
    fun initEditCreateMode() {
        initBotonCamara()

        val btn: Button = root.findViewById(R.id.buttonSiteAddUpdate)

        val btnMail: FloatingActionButton = root.findViewById(R.id.btnGMail)
        val btnFace: FloatingActionButton = root.findViewById(R.id.btnFacebook)
        val btnTwit: FloatingActionButton = root.findViewById(R.id.btnTwitter)
        val btnInsta: FloatingActionButton = root.findViewById(R.id.btnInstagram)

        val cajaSiteName: EditText = root.findViewById(R.id.txtNameSiteSite)
        val cajaLocalizacion: EditText = root.findViewById(R.id.txtSiteSite)
        val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
        val cajaRating: RatingBar = root.findViewById(R.id.ratingBar)

        when (modo) {
            1 -> {
                // Obtiene instancia a Vibrator
                vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                val b1 = BitmapFactory.decodeResource(resources, R.drawable.add)
                images = arrayOf(b1)

                adapter = SliderAdapter(context!!, images)
                val slider: ViewPager = root.findViewById(R.id.imageSite)
                slider.adapter = adapter

                // Fragment de Creacion
                btn.text = getString(R.string.add)

                btnMail.isVisible = false
                btnFace.isVisible = false
                btnTwit.isVisible = false
                btnInsta.isVisible = false

                add(btn)
            }

            2 -> {
                // Fragment de Edicion
                cargarDatosSite()
                btn.text = getString(R.string.update)

                btnMail.isVisible = false
                btnFace.isVisible = false
                btnTwit.isVisible = false
                btnInsta.isVisible = false

                update(btn)
            }

            3 -> {
                // Fragment de Consulta
                cargarDatosSite()
                btn.isVisible = false

                btnMail.isVisible = true
                btnFace.isVisible = true
                btnTwit.isVisible = true
                btnInsta.isVisible = true

                cajaSiteName.isEnabled = false
                cajaLocalizacion.isEnabled = false
                cajafecha.isEnabled = false
                cajaRating.isEnabled = false

            }
        }

    }

    /**
     * Metodo para cargar los datos del Sitio en el Fragment
     */
    private fun cargarDatosSite() {

        val cajaSiteName: EditText = root.findViewById(R.id.txtNameSiteSite)
        val cajaLocalizacion: EditText = root.findViewById(R.id.txtSiteSite)
        val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
        val cajaRating: RatingBar = root.findViewById(R.id.ratingBar)

        cajaSiteName.setText(sitio?.name)
        cajaLocalizacion.setText(sitio?.site)
        cajafecha.setText(sitio?.date)
        cajaRating.rating = ((sitio?.rating)?.toFloat() ?: 0.0) as Float
    }

    /**
     * Metodo para aniadir un sitio
     */
    fun add(btn: Button) {

        btn.setOnClickListener {
            val cajaSiteName: EditText = root.findViewById(R.id.txtNameSiteSite)
            val cajaLocalizacion: EditText = root.findViewById(R.id.txtSiteSite)
            val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
            val cajaRating: RatingBar = root.findViewById(R.id.ratingBar)
            // Recuperamos las fotos subidas
            // image

            name = cajaSiteName.text.toString()
            site = cajaLocalizacion.text.toString()
            date = cajafecha.text.toString()
            rating = cajaRating.rating.toDouble()
//        image = UtilImage.toBase64(imgBtnPhoto.drawable.toBitmap()).toString()
            longitude = 0.0
            latitude = 2.0
            //lugar = Site(name!!, site!!, date!!, rating, latitude, longitude)
            SiteController.insertSite(lugar)
            Toast.makeText(context!!, R.string.site_added, Toast.LENGTH_SHORT).show()
            Log.i("site", lugar.toString())

            // Vibracion
            vibrate()

            // Volvemos a MySites Fragment
            volverMySites()

        }
    }

    /**
     * Metodo para editar un sitio
     */
    private fun update(btn: Button) {

        btn.setOnClickListener {
            val cajaSiteName: EditText = root.findViewById(R.id.txtNameSiteSite)
            val cajaLocalizacion: EditText = root.findViewById(R.id.txtSiteSite)
            val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
            val cajaRating: RatingBar = root.findViewById(R.id.ratingBar)
            // Recuperamos las fotos subidas
            // image

            name = cajaSiteName.text.toString()
            site = cajaLocalizacion.text.toString()
            date = cajafecha.text.toString()
            rating = cajaRating.rating.toDouble()
//        image = UtilImage.toBase64(imgBtnPhoto.drawable.toBitmap()).toString()
            longitude = 0.0
            latitude = 2.0

           // lugar = Site(name!!, image, site!!, date!!, rating, latitude, longitude)

            SiteController.updateSite(lugar)

            Toast.makeText(context!!, R.string.site_modified, Toast.LENGTH_SHORT).show()

            // Volvemos a MySites Fragment
            volverMySites()
        }
    }

    /**
     * Funcion que devuelve al MySitesFragment
     */
    private fun volverMySites() {
        val fragm = MySitesFragment()
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.add(R.id.nav_host_fragment, fragm)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Inicia los eventos de los botones
     */
    private fun initBotonCamara() {
        val btnImg: ViewPager = root.findViewById(R.id.imageSite)
        btnImg.setOnClickListener {
            initDialogFoto()
        }
    }

    /**
     * Muestra el diálogo para tomar foto o elegir de la galería
     */
    private fun initDialogFoto() {
        val fotoDialogoItems = arrayOf(
            getString(R.string.Gallery),
            getString(R.string.Photo)
        )
        //Dialogo para eligir opciones
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.SelectOption))
            .setItems(fotoDialogoItems) { _, modoFoto ->
                when (modoFoto) {
                    0 -> takephotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            .show()
    }

    /**
     * Elige una foto de la galeria
     */
    private fun takephotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    /**
     * Metodo que llama al intent de la camamara para tomar una foto
     */
    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //CAPTURA LA FOTO Y LA METE DETRO DE LA VARIABLE
        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGEN)
        startActivityForResult(intent, CAMERA)
    }

    /**
     * Cuando ejecutamos una actividad y da un resultado
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val btnImg: Button = root.findViewById(R.id.imageSite)

        Log.d("site", "Opción::--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        //Si cancela no hace nada
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("site", "Se ha cancelado")
        }
        //si elige la opcion de galeria entra en la galeria
        if (requestCode == GALLERY) {
            Log.d("site", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI
                val contentURI = data.data!!
                try {
                    val bitmap = differentVersion(contentURI)
                    // btnImg.setImageBitmap(bitmap)//mostramos la imagen

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, getText(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            Log.d("site", "Entramos en Camara")
            //cogemos la imagen
            try {
                val foto = differentVersion(IMAGEN)
                // Mostramos la imagen
                // btnImg.setImageBitmap(foto)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, getText(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Metodo que devuleve un bitmap depende de la version
     * @return un bitmap
     */
    fun differentVersion(contentURI: Uri): Bitmap {
        //Para controlar la version de android usar uno u otro
        val bitmap: Bitmap
        bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context?.contentResolver, contentURI);
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(context!!.contentResolver, contentURI)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap;
    }
    //************************************************************
    //METODO  PARA LOS PERMISOS**********************
    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisos() {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(context)
            //PERMISOS
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.i("sing", "Ha aceptado todos los permisos")
                    }
                    // COMPROBAMOS QUE NO HAY PERMISOS SIN ACEPTAR
                    if (report.isAnyPermissionPermanentlyDenied) {
                    }
                }//NOTIFICAR DE LOS PERMISOS

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
            .onSameThread()
            .check()
    }

    /**
     * Vibrador
     */
    fun vibrate() {
        //Compruebe si dispositivo tiene un vibrador.
        if (vibrator!!.hasVibrator()) { //Si tiene vibrador

            val tiempo: Long = 500 //en milisegundos
            vibrator!!.vibrate(tiempo)

        } else { //no tiene
            //Log.v("VIBRATOR", "Este dispositivo NO puede vibrar");
        }
    }

}
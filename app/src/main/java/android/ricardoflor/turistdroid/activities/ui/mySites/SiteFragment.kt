package android.ricardoflor.turistdroid.activities.ui.mySites

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilImage
import android.transition.Slide
import android.util.Log
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.fragment_site.*
import java.util.*

class SiteFragment(modo: Boolean) : Fragment() {

    private val modo = modo
    private lateinit var root: View

    // Variables Site
    private var lugar: Site = Site()
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

    // Variables Slider Images
    private var images: Array<Int> = arrayOf(R.drawable.ic_ricflor_add_photo)
    private lateinit var adapter: PagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_site, container, false)
        editCreateMode()
        val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
        cajafecha.setOnClickListener {
            showDatePickerDialog()
        }

        adapter = SliderAdapter(context!!, images)
        val slider: ViewPager = root.findViewById(R.id.iamgeSite)
        slider.adapter = adapter

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
    fun editCreateMode() {
        val btn: Button = root.findViewById(R.id.buttonSiteAddUpdate)

        if (modo) {
            // Fragment de Creacion
            btn.text = getString(R.string.add)

            add(btn)
        } else {
            // Fragment de Edicion
            btn.text = getString(R.string.update)

            update(btn)
        }

    }

    /**
     * Metodo para aniadir un sitio
     */
    fun add(btn: Button) {

        btn.setOnClickListener {
            // lugar.name = root.findViewById(R.id.txtNameSiteSite)
            val cajaSiteName: EditText = root.findViewById(R.id.txtNameSiteSite)
            val cajaLocalizacion: EditText = root.findViewById(R.id.txtSiteSite)
            val cajafecha: EditText = root.findViewById(R.id.txtDateSite)
            val cajaRating: RatingBar = root.findViewById(R.id.ratingBar)
//        image

            lugar.name = cajaSiteName.text.toString()
            lugar.site = cajaLocalizacion.text.toString()
            lugar.date = cajafecha.text.toString()
            lugar.rating = cajaRating.rating.toDouble()
//        lugar.image = UtilImage.toBase64(imgBtnPhoto.drawable.toBitmap()).toString()
//        SiteController.insertSite(lugar)

            Toast.makeText(context!!, "AÑADO", Toast.LENGTH_SHORT).show()
            Log.i("site", lugar.toString())
            //SiteController.insertSite(lugar)

        }
    }

    /**
     * Metodo para editar un sitio
     */
    private fun update(btn: Button) {

        btn.setOnClickListener {
            Toast.makeText(context!!, "EDITO", Toast.LENGTH_SHORT).show()


            //SiteController.insertSite(lugar)

        }
    }
// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------------------------------------------------
//    /**
//     * Inicia los eventos de los botones
//     */
//    private fun initBotones() {
//        imgBtnPhoto.setOnClickListener {
//            initDialogFoto()
//        }
//    }
//
//    /**
//     * Muestra el diálogo para tomar foto o elegir de la galería
//     */
//    private fun initDialogFoto() {
//        val fotoDialogoItems = arrayOf(
//            getString(R.string.Gallery),
//            getString(R.string.Photo)
//        )
//        //Dialogo para eligir opciones
//        AlertDialog.Builder(this)
//            .setTitle(getString(R.string.SelectOption))
//            .setItems(fotoDialogoItems) { _, modo ->
//                when (modo) {
//                    0 -> takephotoFromGallery()
//                    1 -> takePhotoFromCamera()
//                }
//            }
//            .show()
//    }
//
//    /**
//     * Elige una foto de la galeria
//     */
//    private fun takephotoFromGallery() {
//        val galleryIntent = Intent(
//            Intent.ACTION_PICK,
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        )
//        startActivityForResult(galleryIntent, GALLERY)
//    }
//
//    /**
//     * Metodo que llama al intent de la camamara para tomar una foto
//     */
//    private fun takePhotoFromCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        //CAPTURA LA FOTO Y LA METE DETRO DE LA VARIABLE
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE)
//        startActivityForResult(intent, CAMERA)
//    }
//
//    /**
//     * Cuando ejecutamos una actividad y da un resultado
//     * @param requestCode Int
//     * @param resultCode Int
//     * @param data Intent?
//     */
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        Log.d("sing", "Opción::--->$requestCode")
//        super.onActivityResult(requestCode, resultCode, data)
//        //Si cancela no hace nada
//        if (resultCode == RESULT_CANCELED) {
//            Log.d("sing", "Se ha cancelado")
//        }
//        //si elige la opcion de galeria entra en la galeria
//        if (requestCode == GALLERY) {
//            Log.d("sing", "Entramos en Galería")
//            if (data != null) {
//                // Obtenemos su URI
//                val contentURI = data.data!!
//                try {
//                    val bitmap = differentVersion(contentURI)
//                    imgBtnPhoto.setImageBitmap(bitmap)//mostramos la imagen
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    Toast.makeText(this, getText(R.string.error_gallery), Toast.LENGTH_SHORT).show()
//                }
//            }
//        } else if (requestCode == CAMERA) {
//            Log.d("sing", "Entramos en Camara")
//            //cogemos la imagen
//            try {
//                val foto = differentVersion(IMAGE)
//                // Mostramos la imagen
//                imgBtnPhoto.setImageBitmap(foto)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this, getText(R.string.error_camera), Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    /**
//     * Metodo que devuleve un bitmap depende de la version
//     * @return un bitmap
//     */
//    fun differentVersion(contentURI: Uri): Bitmap {
//        //Para controlar la version de android usar uno u otro
//        val bitmap: Bitmap
//        bitmap = if (Build.VERSION.SDK_INT < 28) {
//            MediaStore.Images.Media.getBitmap(contentResolver, contentURI);
//        } else {
//            val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI)
//            ImageDecoder.decodeBitmap(source)
//        }
//        return bitmap;
//    }
//    //************************************************************
//    //METODO  PARA LOS PERMISOS**********************
//    /**
//     * Comprobamos los permisos de la aplicación
//     */
//    private fun initPermisos() {
//        //ACTIVIDAD DONDE TRABAJA
//        Dexter.withContext(this)
//            //PERMISOS
//            .withPermissions(
//                Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//            )//LISTENER DE MULTIPLES PERMISOS
//            .withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
//                    if (report.areAllPermissionsGranted()) {
//                        Log.i("sing", "Ha aceptado todos los permisos")
//                    }
//                    // COMPROBAMOS QUE NO HAY PERMISOS SIN ACEPTAR
//                    if (report.isAnyPermissionPermanentlyDenied) {
//                    }
//                }//NOTIFICAR DE LOS PERMISOS
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permissions: List<PermissionRequest?>?,
//                    token: PermissionToken
//                ) {
//                    token.continuePermissionRequest()
//                }
//            }).withErrorListener {
//                Toast.makeText(
//                    applicationContext,
//                    getString(R.string.error_permissions),
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            .onSameThread()
//            .check()
//    }
}
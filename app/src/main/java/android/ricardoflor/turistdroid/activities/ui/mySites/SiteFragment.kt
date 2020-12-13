package android.ricardoflor.turistdroid.activities.ui.mySites

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.Vibrator
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.image.ImageController
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.ricardoflor.turistdroid.utils.UtilImage
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.fragment_site.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class SiteFragment(modo: Int, site: Site?) : Fragment() {

    private val modo = modo
    private val sitio: Site? = site
    private lateinit var root: View

    // Botones y Cajas de Texto
    private var btnAddUpdate: Button? = null
    private var btnAddImage: FloatingActionButton? = null
    private var btnMail: FloatingActionButton? = null
    private var btnFace: FloatingActionButton? = null
    private var btnTwit: FloatingActionButton? = null
    private var btnInsta: FloatingActionButton? = null
    private var cajaSiteName: EditText? = null
    private var cajaLocalizacion: Spinner? = null
    private var cajaFecha: EditText? = null
    private var cajaRating: RatingBar? = null

    // Variables Site
    private lateinit var lugar: Site
    private var name: String? = null
    private var image: RealmList<Image> = RealmList()
    private var site: String? = null
    private var date: String? = null
    private var rating: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // Variables Camara
    private val GALLERY = 1
    private val CAMERA = 2
    private lateinit var IMAGEN_NOMBRE: String
    private val IMAGEN_DIR = "/TuristDroid"
    lateinit var IMAGE: Uri
    private lateinit var FOTO: Bitmap
    private var imagenIni: Boolean = true
    private var idFoto: Long = 1
    private lateinit var qrShare: Bitmap

    // Variables Slider Images
    private var imagesSlider: RealmList<Bitmap> = RealmList()
    private lateinit var adapter: PagerAdapter

    // Vibrador
    private var vibrator: Vibrator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_site, container, false)
        init()

        return root
    }

    private fun init() {
        initButtons()
        initEditCreateMode()

        cajaFecha?.setOnClickListener { showDatePickerDialog() }
        btnMail?.setOnClickListener { shareGmail() }
        btnFace?.setOnClickListener { shareSite("com.facebook.katana") }
        btnTwit?.setOnClickListener { shareSite("com.twitter.android") }
        btnInsta?.setOnClickListener { shareSite("com.instagram.android") }
    }

    /**
     * Funcion para compartir con Gmail
     */
    private fun shareGmail() {
        val uri = getImageUri(requireContext(), qrShare)
        val intent = Intent().apply {
            Intent(Intent.ACTION_SENDTO)
            data = Uri.parse("mailto:" )
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_mysite))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_mysite))
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    /**
     * Funcion para compartir con Twitter, Facebook e Instagram
     */
    fun shareSite(str: String) {
        val msg: String = getString(R.string.share_mysite)
        val uri = getImageUri(requireContext(), qrShare)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, msg)
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
            setPackage(str)
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    /**
     * Funcion que coge la Uri de una imagen Bitmap
     */
    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCamaraQr()
    }

    private fun initCamaraQr() {
        initBotonesCamara()
        initPermisos()
    }

    /**
     * Inicializamos todos los botones
     */
    private fun initButtons() {
        btnAddUpdate = root.findViewById(R.id.buttonSiteAddUpdate)
        btnMail = root.findViewById(R.id.btnGMail)
        btnFace = root.findViewById(R.id.btnFacebook)
        btnTwit = root.findViewById(R.id.btnTwitter)
        btnInsta = root.findViewById(R.id.btnInstagram)
        cajaSiteName = root.findViewById(R.id.txtNameSiteSite)
        cajaLocalizacion = root.findViewById(R.id.txtSiteSite)
        cajaFecha = root.findViewById(R.id.txtDateSite)
        cajaRating = root.findViewById(R.id.ratingBar)
        btnAddImage = root.findViewById(R.id.addImageButton)
    }

    /**
     * Funcion que muestra un DatePicker
     */
    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(this.parentFragmentManager, "datePicker")
    }

    private fun onDateSelected(day: Int, month: Int, year: Int) {
        cajaFecha?.setText("$day/$month/$year")
    }

    /**
     * Metodo para abrir el fragment en edicion o en creacion
     */
    private fun initEditCreateMode() {

        //Obtiene el ultimo ID de las imagenes de la BD
        idFoto = ImageController.getIdImage()

        when (modo) {
            1 -> { // Fragment de Creacion
                // Obtiene instancia a Vibrator
                vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                // Slider Images
                val b1 = BitmapFactory.decodeResource(resources, R.drawable.add)
                imagesSlider.add(b1)

                adapter = SliderAdapter(context!!, imagesSlider)
                val slider: ViewPager = root.findViewById(R.id.imageSite)
                slider.adapter = adapter

                btnAddUpdate?.text = getString(R.string.add)
                mostrarBotonesSocial(false)
                add(btnAddUpdate!!)
            }

            2 -> { // Fragment de Edicion
                cargarDatosSite()
                btnAddUpdate?.text = getString(R.string.update)
                mostrarBotonesSocial(false)
                update(btnAddUpdate!!)
            }

            3 -> { // Fragment de Consulta
                cargarDatosSite()
                btnAddUpdate?.isVisible = false
                btnAddImage?.isVisible = false
                mostrarBotonesSocial(true)

                cajaSiteName?.isEnabled = false
                cajaLocalizacion?.isEnabled = false
                cajaFecha?.isEnabled = false
                cajaRating?.isEnabled = false

            }
        }
    }

    private fun mostrarBotonesSocial(bool: Boolean) {
        btnMail?.isVisible = bool
        btnFace?.isVisible = bool
        btnTwit?.isVisible = bool
        btnInsta?.isVisible = bool
    }

    /**
     * Metodo para cargar los datos del Sitio en el Fragment
     */
    private fun cargarDatosSite() {
        cajaSiteName?.setText(sitio?.name)

        // Cargamos el spinner con la opcion correcta
        var lista: Array<String> = resources.getStringArray(R.array.sites_types)

        var opc: Int = 0

        for (it in lista) {
            if (it.equals(sitio?.site.toString())) {
                break
            }
            opc++
        }
        cajaLocalizacion?.setSelection(opc)

        cajaFecha?.setText(sitio?.date)
        cajaRating?.rating = ((sitio?.rating)?.toFloat() ?: 0.0) as Float

        //Rellena la lista con las imagenes de la BD
        for (img in sitio!!.image) {
            imagesSlider.add(UtilImage.toBitmap(img!!.image))
        }

        adapter = SliderAdapter(context!!, imagesSlider)
        val slider: ViewPager = root.findViewById(R.id.imageSite)
        slider.adapter = adapter

        // TODO Falta informacion del mapa - longi y latit ---------------------------------------------------------------------
        var textoQr: String = sitio?.name + ";" + opc + ";" + sitio?.date + ";" + (sitio?.rating)?.toFloat()

        generateQRCode(textoQr)
    }

    /**
     * Metodo para cargar los datos del Sitio en el Fragment
     */
    private fun cargarDatosSiteQr(text: String) {

        val parts = text.split(";")

        cajaSiteName?.setText(parts[0])
        cajaLocalizacion?.setSelection(parts[1].toInt())
        cajaFecha?.setText(parts[2])
        cajaRating?.rating = ((parts[3])?.toFloat() ?: 0.0) as Float

        // TODO Falta informacion del mapa - longi y latit ---------------------------------------------------------------------
    }

    /**
     * Metodo para aniadir un sitio
     */
    fun add(btn: Button) {

        btn.setOnClickListener {
            // Recuperamos los datos
            // image
            name = cajaSiteName?.text.toString()
            site = cajaLocalizacion?.selectedItem.toString()
            date = cajaFecha?.text.toString()
            rating = cajaRating?.rating?.toDouble() ?: 0.0

            longitude = 0.0
            latitude = 2.0

            lugar = Site(name!!, image, site!!, date!!, rating, latitude, longitude)

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
            // Recuperamos las fotos subidas
            // image
            name = cajaSiteName?.text.toString()
            site = cajaLocalizacion?.selectedItem.toString()
            date = cajaFecha?.text.toString()
            rating = cajaRating?.rating?.toDouble() ?: 0.0

            longitude = 0.0
            latitude = 2.0

            lugar = Site(name!!, image, site!!, date!!, rating, latitude, longitude)

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

//**************************************************************************
    //METODO PARA LOS PERMISOS DE LA GESTION DE LA CAMARA **********************
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
                Toast.makeText(
                    context?.applicationContext,
                    getString(R.string.error_permissions),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()


    }

    //************************************************************
    //METODOS PARA LA IMAGEN**************************************
    /**
     * Inicia los eventos de los botones
     */
    private fun initBotonesCamara() {
        // Solo se puede escanear un QR en el Modo Creacion
        if (modo == 1) {
            qrView.setOnClickListener {
                scanQRCode()
            }
        }
        addImageButton.setOnClickListener {
            initDialogFoto()
        }
    }

    /**
     * Ecanea un codigo QR
     */
    private fun scanQRCode() {
        val integrator = IntentIntegrator.forSupportFragment(this).apply {
            captureActivity = CaptureActivity::class.java
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        }
        integrator.initiateScan()
    }

    /**
     * Genera una imagen QR desde un texto dado y la pinta en la pantalla
     */
    private fun generateQRCode(text: String) {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            //Pasa la imagen al objeto en pantalla
            val qrImg: ImageView = root.findViewById(R.id.qrView)
            qrImg.setImageBitmap(bitmap)
            // Almacenamos el QR para compartirlo en Redes
            qrShare = bitmap

        } catch (e: WriterException) {
            Log.d("QR", "Error QR")
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
            .setItems(fotoDialogoItems) { _, modo ->
                when (modo) {
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
        // Si queremos hacer uso de fotos en alta calidad
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Eso para alta o baja
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = UtilImage.crearNombreFichero()
        // Salvamos el fichero
        val fichero = UtilImage.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, context!!)
        IMAGE = Uri.fromFile(fichero)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE)
        // Esto para alta y baja
        startActivityForResult(intent, CAMERA)
    }

    /**
     * Cuando ejecutamos una actividad y da un resultado
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {

            //Recupera la informacion si ha escaneado un QR
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                //Si no tiene informacion se cancela
                if (result.contents == null) {
                    Toast.makeText(context, getText(R.string.error), Toast.LENGTH_LONG).show()
                } else {
                    //Carga la informacion obtenida del QR
                    cargarDatosSiteQr(result.contents)
                }
            }

            //Si cancela no hace nada
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                Log.d("sing", "Se ha cancelado")
            }
            //si elige la opcion de galeria entra en la galeria
            if (requestCode == GALLERY) {
                Log.d("profile", "Entramos en Galería")
                if (data != null) {
                    // Obtenemos su URI
                    val contentURI = data.data!!
                    try {
                        FOTO = differentVersion(contentURI)

                        val imgStr = UtilImage.toBase64(FOTO)!!
                        idFoto += 1
                        val img = Image(idFoto, imgStr)
                        image.add(img)

                        //Para borrar la imagen de muestra
                        if (modo == 1 && imagenIni) {
                            imagesSlider = RealmList()
                            imagenIni = false
                        }

                        imagesSlider.add(FOTO)
                        adapter = SliderAdapter(context!!, imagesSlider)
                        val slider: ViewPager = root.findViewById(R.id.imageSite)
                        slider.adapter = adapter
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(context!!, getText(R.string.error_gallery), Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                Log.d("profile", "Entramos en Camara")
                //cogemos la imagen
                try {
                    FOTO = differentVersion(IMAGE)
                    // Mostramos la imagen

                    val imgStr = UtilImage.toBase64(FOTO)!!
                    idFoto += 1
                    val img = Image(idFoto, imgStr)
                    image.add(img)

                    //Para borrar la imagen de muestra
                    if (modo == 1 && imagenIni) {
                        imagesSlider = RealmList()
                        imagenIni = false
                    }

                    imagesSlider.add(FOTO)
                    adapter = SliderAdapter(context!!, imagesSlider)
                    val slider: ViewPager = root.findViewById(R.id.imageSite)
                    slider.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context!!, getText(R.string.error_camera), Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(context, "No se ha podido cargar la imagen", 5)
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
            val source: ImageDecoder.Source = ImageDecoder.createSource(context?.contentResolver!!, contentURI)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap;
    }

    // Metodos de Validaciones --------------------------------------------------------------------------------------
    /**
     * Metodo que devuelve false si alguno de los valores esta vacio
     */
    private fun anyEmpty(): Boolean {
        var valid = true
        if (notEmpty(cajaSiteName!!) && notEmpty(txtEmail) && notEmpty(txtPass) && notEmpty(txtUserName)) {
            valid = false
        }
        return valid
    }

    /**
     * Método que comprueba si el campo esta vacio y lanza un mensaje
     * @param txt TextView
     */
    private fun notEmpty(txt: TextView): Boolean {
        var empty = false
        if (txt.text.isEmpty()) {
            txt.error = resources.getString(R.string.isEmpty)
            empty = true
        }
        return empty
    }
}
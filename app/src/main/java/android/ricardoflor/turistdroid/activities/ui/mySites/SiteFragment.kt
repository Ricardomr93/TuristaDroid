package android.ricardoflor.turistdroid.activities.ui.mySites

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.location.Location
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
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.ricardoflor.turistdroid.utils.UtilImage
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.fragment_site.*
import java.io.IOException
import java.util.ArrayList

class SiteFragment(modo: Int, site: Site?) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Variables a usar y permisos del mapa
    private lateinit var mMap: GoogleMap
    private lateinit var myPosition: FusedLocationProviderClient
    private var marker: Marker? = null
    private var location: Location? = null
    private var posicion: LatLng? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var positionSite :LatLng

    private val modo = modo
    private val sitio: Site? = site
    private lateinit var root: View

    // Botones y Cajas de Texto
    private var btnAddUpdate: Button? = null
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
        initMap()
        myActualPosition()

        cajaFecha?.setOnClickListener {
            showDatePickerDialog()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCamara()
    }

    private fun initCamara() {
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
    // Metodos de Validaciones --------------------------------------------------------------------------------------
    /**
     * Metodo que devuelve false si alguno de los valores esta vacio
     */
    private fun anyEmpty(): Boolean {
        var valid = true
        if (notEmpty(txtDateSite) && notEmpty(txtNameSiteSite)) {
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

    /**
     * Metodo para abrir el fragment en edicion o en creacion
     */
    private fun initEditCreateMode() {

        when (modo) {
            1 -> {
                // Obtiene instancia a Vibrator
                vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                // Slider Images
                val b1 = BitmapFactory.decodeResource(resources, R.drawable.add)
                imagesSlider.add(b1)

                adapter = SliderAdapter(context!!, imagesSlider)
                val slider: ViewPager = root.findViewById(R.id.imageSite)
                slider.adapter = adapter

                // Fragment de Creacion
                btnAddUpdate?.text = getString(R.string.add)
                mostrarBotonesSocial(false)
                add(btnAddUpdate!!)
            }

            2 -> {
                // Fragment de Edicion
                cargarDatosSite()
                btnAddUpdate?.text = getString(R.string.update)
                mostrarBotonesSocial(false)
                update(btnAddUpdate!!)
            }

            3 -> {
                // Fragment de Consulta
                cargarDatosSite()
                btnAddUpdate?.isVisible = false
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
        positionSite = LatLng(sitio!!.latitude, sitio!!.longitude)
    }

    /**
     * Metodo para aniadir un sitio
     */
    fun add(btn: Button) {

        btn.setOnClickListener {


            if(anyEmpty()){
                // Recuperamos los datos
                // image
                name = cajaSiteName?.text.toString()
                site = cajaLocalizacion?.selectedItem.toString()
                date = cajaFecha?.text.toString()
                rating = cajaRating?.rating?.toDouble() ?: 0.0
//        image = UtilImage.toBase64(imgBtnPhoto.drawable.toBitmap()).toString()
                if (posicion != null) {
                    latitude = posicion!!.latitude
                    longitude = posicion!!.longitude
                    lugar = Site(name!!, image, site!!, date!!, rating, latitude, longitude)
                    SiteController.insertSite(lugar)
                    Toast.makeText(context!!, R.string.site_added, Toast.LENGTH_SHORT).show()
                    Log.i("site", lugar.toString())
                    // Vibracion
                    vibrate()
                    // Volvemos a MySites Fragment
                    volverMySites()
                } else {
                    Toast.makeText(context!!, R.string.needPosition, Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context!!, R.string.isEmpty, Toast.LENGTH_SHORT).show()
            }

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
//        image = UtilImage.toBase64(imgBtnPhoto.drawable.toBitmap()).toString()
            if (posicion != null) {
                latitude = posicion!!.latitude
                longitude = posicion!!.longitude
                lugar = Site(name!!, image, site!!, date!!, rating, latitude, longitude)
                SiteController.updateSite(lugar)
                Toast.makeText(context!!, R.string.site_modified, Toast.LENGTH_SHORT).show()
                // Volvemos a MySites Fragment
                volverMySites()
            } else {
                Toast.makeText(context!!, R.string.needPosition, Toast.LENGTH_SHORT).show()
            }
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
    //METODO PARA LOS PERMISOS **********************
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
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
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
        addImageButton.setOnClickListener {
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
            .setItems(fotoDialogoItems) { _, mode ->
                when (mode) {
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
                        val id = 0
                        val img = Image(id.toLong(), imgStr)
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
                    val id = 0
                    val img = Image(id.toLong(), imgStr)
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


    //************************************************************
    //METODOS MAP*************************************************
    /**
     * metodo que inicia el mapa
     */
    private fun initMap() {
        val mapFragment = (childFragmentManager
            .findFragmentById(R.id.mapViewAddSite) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }


    /**
     * Configuración del mapa con zoom
     */
    private fun configurarIUMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        typeMap()

    }

    private fun typeMap() {

        val uiSettings = mMap.uiSettings
        when (modo) {
            1, 2 -> {
                uiSettings.isRotateGesturesEnabled = true
            }
            3 -> {
                uiSettings.isRotateGesturesEnabled = false
                uiSettings.isCompassEnabled = false
                uiSettings.isMapToolbarEnabled = false
                uiSettings.isIndoorLevelPickerEnabled = false
                uiSettings.isZoomControlsEnabled = false
                uiSettings.isMyLocationButtonEnabled = false
                uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
                uiSettings.isScrollGesturesEnabled = false
                mMap.setMinZoomPreference(16.0f)
                positionShow()
            }
        }
    }

    /**
     * Metodo que se inicia cuando el mapa está listo
     * @param maps : GoogleMap
     */
    override fun onMapReady(maps: GoogleMap) {
        mMap = maps
        mMap.isMyLocationEnabled = true
        getPosition()
        locationReq()
        getLatitudeOnClick()
        configurarIUMapa()
    }

    /**
     * metodo cuando pulsas un marcador
     * @param marker : Marker
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    /**
     * Metodo que coge la posicion al pulsar y pinta un marcador
     */
    private fun getLatitudeOnClick() {
        mMap.setOnMapClickListener { lat ->
            posicion = LatLng(lat.latitude, lat.longitude)
            markCurrentPostition(posicion!!)
            Toast.makeText(context!!, posicion.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Metodo que pinta un marcador en la posicion indicada
     */
    private fun markCurrentPostition(loc: LatLng) {
        val icon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                .decodeResource(context?.resources, R.drawable.ic_marker)
        )
        marker?.remove()//borra el marcardor si existe
        marker = mMap.addMarker(
            MarkerOptions()
                .position(loc) // posicion
                .icon(icon)
        )
    }

    /**
     * Metodo que muestra la ubicacion del sitio y mueve la camara
     */
    fun positionShow() {
        sitePosition()
    }
    /**
     * Metodo que mueve la camara hasta la posicion actual
     */
    private fun cameraMapSite(position : LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
    }

    /**
     * Posicion del sitio
     */
    private fun sitePosition() {
        if (this::positionSite.isInitialized){
            val icon = BitmapDescriptorFactory.fromBitmap(
                BitmapFactory
                    .decodeResource(context?.resources, R.drawable.ic_marker)
            )
            //marker?.remove()//borra el marcardor si existe
            marker = mMap.addMarker(
                MarkerOptions()
                    .position(positionSite!!) // posicion
                    .icon(icon)
            )
            cameraMapSite(positionSite)
        }

    }
    //************************************************************
    //METODOS GPS*************************************************
    /**
     * Metodo que recoge la ubicacion actual y la mete en una variable
     */
    private fun myActualPosition() {
        myPosition = LocationServices.getFusedLocationProviderClient(activity!!)
    }

    /**
     * Metodo que actualiza la posicion
     */
    fun locationReq() {
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10 * 1000)        // 10 segundos en milisegundos
            .setFastestInterval(1 * 1000) // 1 segundo en milisegundos
    }

    /**
     * Obtiene la posicion
     */
    private fun getPosition() {
        Log.i("Mape", "Obteniendo posición")
        try {
            val local: Task<Location> = myPosition.lastLocation
            local.addOnCompleteListener(
                activity!!
            ) { task ->
                if (task.isSuccessful) {
                    // Actualizamos la última posición conocida
                    location = task.result
                    if (location != null) {
                        posicion = LatLng(
                            location!!.latitude,
                            location!!.longitude
                        )
                        cameraMap()
                    }
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                view!!,
                "No se ha encontrado su posoción actual o el GPS está desactivado",
                Snackbar.LENGTH_LONG
            ).show();
        }
    }

    /**
     * Metodo que mueve la camara hasta la posicion actual
     */
    private fun cameraMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion))
    }


}
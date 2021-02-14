package android.ricardoflor.turistdroid.activities.ui.nexttome

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.ricardoflor.turistdroid.MyApplication
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.NavigationActivity
import android.ricardoflor.turistdroid.activities.ui.mySites.MySitesFragment
import android.ricardoflor.turistdroid.activities.ui.mySites.SiteFragment
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.image.ImageDTO
import android.ricardoflor.turistdroid.bd.image.ImageMapper
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteDTO
import android.ricardoflor.turistdroid.bd.site.SiteMapper
import android.ricardoflor.turistdroid.utils.UtilImage
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObjects
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_next_to_me.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class NextToMeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Variables a usar y permisos del mapa
    private lateinit var mMap: GoogleMap
    private lateinit var myPosition: FusedLocationProviderClient
    private var location: Location? = null
    private var posicion: LatLng? = null
    private var locationRequest: LocationRequest? = null
    private var DISTANCE = 1.0//en Km

    //private var img: String = ""
    private var imagesSlider: MutableList<Image> = mutableListOf()

    // Cloud Firestore
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_next_to_me, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { view, motionEvent ->
            return@setOnTouchListener true
        }
        init()
    }

    private fun init() {
        if (initPermisos()) {
            cargarImagenes()
            initMap()
            myActualPosition()
        } else {
            Toast.makeText(
                context?.applicationContext,
                getString(R.string.need_location),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    //************************************************************
    //METODOS MAP*************************************************
    /**
     * metodo que inicia el mapa
     */
    private fun initMap() {
        val mapFragment = (childFragmentManager
            .findFragmentById(R.id.mapNextToMe) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    /**
     * Configuración del mapa con zoom
     */
    private fun configurarIUMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val uiSettings = mMap.uiSettings
        mMap.setOnMarkerClickListener(this)//al pulsas un marker
        uiSettings.isCompassEnabled = true
    }

    /**
     * Metodo que se inicia cuando el mapa está listo
     * @param maps : GoogleMap
     */
    override fun onMapReady(maps: GoogleMap) {
        mMap = maps
        mMap.isMyLocationEnabled = true
        configurarIUMapa()
        getPosition()
        locationReq()
        clickOnInfoWIndow()
        changeDistance()
    }

    /**
     * metodo cuando pulsas un marcador
     * @param marker : Marker
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        infoWindow()
        return false
    }

    /**
     * Metodo que pinta un marcador en la posicion indicada
     */
    private fun markCurrentPostition(loc: LatLng, site: Site) {
        val icon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                .decodeResource(context?.resources, R.drawable.ic_marker)
        )
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(loc) // posicion
                .title(site.name)
                .snippet(site.site)
                .icon(icon)
        )
        marker.tag = site // para recuperarlo al pulsar
    }

    /**
     * Actualiza la camara para que se vean todos los marcadores
     * @param listaLugares MutableList<Site>?
     */
    private fun allSeeMarker(listaLugares: MutableList<Site>?) {
        val bc = LatLngBounds.Builder()
        for (item in listaLugares!!) {
            bc.include(LatLng(item.latitude, item.longitude))
        }
        bc.include(LatLng(posicion!!.latitude, posicion!!.longitude))
        //si no encuentra ninguno no entra
        if (listaLugares.size > 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 120))
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15f))
            Toast.makeText(
                context?.applicationContext,
                getString(R.string.not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun changeDistance() {
        seekBarNextToMe.progress = 1
        seekBarNextToMe.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                DISTANCE = (progress + 1).toDouble()
                val prog = progress + 1
                txtKmNextToMe.text = "$prog Km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                addMarkerSite()
            }
        })
    }

    /**
     * Calcula la distancia entre dos coordenadas y devuelve la longitud en Km
     */
    fun distanciaCoord(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val radioTierra = 6371.0 //en kilómetros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val va1 = Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)))
        val va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1))
        return radioTierra * va2
    }

    /**
     * Metodo que muestra todos los lugares cercanos a la posicion dada por parametro
     * @param loc :LatLng
     */
    private fun addMarkerSite() {
        mMap.clear()
        val nearme = mutableListOf<Site>()
        db.collection("sites").get().addOnSuccessListener {
            for (item in it) {
                val site = item.toObject(Site::class.java)
                if (posicion != null) {
                    val distance =
                        distanciaCoord(
                            posicion!!.latitude, posicion!!.longitude,
                            site.latitude, site.longitude
                        )
                    if (distance <= DISTANCE) {
                        Log.i("Mapa", "${site.name}: $distance")
                        nearme.add(site)
                        val loc = LatLng(site.latitude, site.longitude)
                        markCurrentPostition(loc, site)
                    }
                }
            }
            allSeeMarker(nearme)
        }
    }
    /**
     * Metodo que muestra una burbuja de dialogo con el nombre foto y puntuacion
     */
    private fun infoWindow() {
        //TODO
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }
            override fun getInfoContents(marker: Marker): View {
                val row: View = layoutInflater.inflate(R.layout.site_marker_dialog, null)
                val txtNamePlaceInfo: TextView = row.findViewById(R.id.txtmakerdialoname)
                val ratin: TextView = row.findViewById(R.id.txtmakerdialograting)
                val imaPlaceInfo: ImageView = row.findViewById(R.id.imgmakerdialog)
                val site = marker.tag as Site
                txtNamePlaceInfo.text = site.name
                ratin.text = String.format("%.1f", (site.rating / site.votos.size))
                if (site.images.isNotEmpty()){
                    Picasso.get().load(site.images[0]).into(imaPlaceInfo)
                }
                return row
            }
        })
    }
    /**
     * Metodo encargado de buscar y rellenar las imagenes en el slaider
     */
    private fun cargarImagenes() {
/*
        var listaImg: MutableList<Image>? = null
        val turistREST = TuristAPI.service
        val call: Call<List<ImageDTO>> = turistREST.imageGetAll()
        call.enqueue(object : Callback<List<ImageDTO>> {
            override fun onResponse(call: Call<List<ImageDTO>>, response: Response<List<ImageDTO>>) {
                if (response.isSuccessful) {
                    imagesSlider =
                        ImageMapper.fromDTO(response.body() as MutableList<ImageDTO>) as MutableList<Image>//saca todos los resultados
                }
            }

            override fun onFailure(call: Call<List<ImageDTO>>, t: Throwable) {
                /*Toast.makeText(
                    applicationContext,
                    getText(R.string.service_error).toString() + t.localizedMessage,
                    Toast.LENGTH_LONG
                )
                    .show()*/
            }
        })
*/
    }

    /**
     * Método que al hacer click en el cuadro de dialogo abre el sitio
     */
    private fun clickOnInfoWIndow() {
        if (this::mMap.isInitialized) {
            mMap.setOnInfoWindowClickListener {
                val site = it.tag as Site
                openSite(site, 3)
            }
        }

    }

    /**
     * Método que abre un fragment sitio
     */
    private fun openSite(site: Site, modo: Int) {
        val addSites = SiteFragment(modo, site)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.nav_host_fragment, addSites)
        transaction.commit()
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
    private fun locationReq() {
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
                        Log.i("Mapa", "Posicion acutal: $posicion")
                        addMarkerSite()
                    }
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                view!!,
                "No se ha encontrado su posoción actual o el GPS está desactivado",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    //************************************************************
    //METODOS PERMISOS********************************************
    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisos(): Boolean {
        var permiss = true
        if (!(activity!!.application as MyApplication).initPermissesLocation()) {
            permiss = false
        }
        return permiss
    }

    private fun goMySites() {

        (activity as NavigationActivity?)!!.isEventoFila = true
        val fragm = MySitesFragment()
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.add(R.id.nav_host_fragment, fragm)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
package android.ricardoflor.turistdroid.activities.ui.nexttome

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.ui.mySites.SiteFragment
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class NextToMeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Variables a usar y permisos del mapa
    private lateinit var mMap: GoogleMap
    private lateinit var myPosition: FusedLocationProviderClient
    private var location: Location? = null
    private var posicion: LatLng? = null
    private var locationRequest: LocationRequest? = null
    private var DISTANCE = 1.050000


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
        initPermisos()
        initMap()
        myActualPosition()
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
    }

    /**
     * metodo cuando pulsas un marcador
     * @param marker : Marker
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        val site = marker.tag as Site
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
        //si no encuentra ninguno no entra
        if(listaLugares.size > 0){
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 120))
        }
    }

    /**
     * Metodo que muestra todos los lugares cercanos a la posicion dada por parametro
     * @param loc :LatLng
     */
    private fun addMarkerSite(loc: LatLng) {
        Log.i("mape", loc.latitude.toString() + "-+" + loc.longitude.toString())
        var listSites = SiteController.selectByNear(loc.latitude, loc.longitude, DISTANCE)
        //var listSites = SiteController.selectAllSite()
        Log.i("mape", listSites.toString())
        //si hay lugares los pinta
        if (listSites != null) {
            for (site in listSites) {
                Log.i("mape", site.toString())
                var loc = LatLng(site.latitude, site.longitude)
                markCurrentPostition(loc, site)
            }
            allSeeMarker(listSites)
        }else{
            Log.i("mape", "ningun lugar cercano)")
        }
    }

    /**
     * Metodo que muestra una burbuja de dialogo con el nombre foto y puntuacion
     */
    private fun infoWindow(){
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter{
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }
            override fun getInfoContents(marker: Marker): View {
                val row: View = layoutInflater.inflate(R.layout.site_marker_dialog, null)
                val txtNamePlaceInfo: TextView = row.findViewById(R.id.txtmakerdialoname)
                val ratin : TextView = row.findViewById(R.id.txtmakerdialograting)
                //val imaPlaceInfo: ImageView = row.findViewById(R.id.imaPlace_infoWindow)
                val site =  marker.tag as Site
                txtNamePlaceInfo.text = site.name
                ratin.text = site.rating.toString()
                //imaPlaceInfo.setImageBitmap(Utilities.base64ToBitmap(place.imagenes[0]!!.foto))
                return row
            }
        })
    }
    private fun clickOnInfoWIndow(){
        if (this::mMap.isInitialized){
            mMap.setOnInfoWindowClickListener {
                val site = it.tag as Site
                openSite(site,3)
            }
        }

    }
    private fun openSite(site: Site, modo: Int){
        val addSites = SiteFragment(modo, site)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.nav_host_fragment, addSites)
        transaction.addToBackStack(null)
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
                        addMarkerSite(posicion!!)
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

    //************************************************************
    //METODOS PERMISOS********************************************
    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisos() {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(context)
            //PERMISOS
            .withPermissions(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.i("mape", "Ha aceptado todos los permisos")
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

}
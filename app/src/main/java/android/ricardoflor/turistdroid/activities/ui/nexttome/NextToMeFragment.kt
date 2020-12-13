package android.ricardoflor.turistdroid.activities.ui.nexttome

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
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
    private var marker: Marker? = null
    private var location: Location? = null
    private var posicion: LatLng? = null
    private var locationRequest: LocationRequest? = null
    private var DISTANCE = 0.050000


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
        anadirLugares()
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
        uiSettings.isCompassEnabled = true
        mMap.setMinZoomPreference(16.0f)//zoom maximo
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
    }

    /**
     * metodo cuando pulsas un marcador
     * @param marker : Marker
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        TODO()
    }
    fun goToSite(){

    }

    /**
     * Metodo que pinta un marcador en la posicion indicada
     */
    private fun markCurrentPostition(loc: LatLng, site: Site) {
        val icon = BitmapDescriptorFactory.fromBitmap(
            BitmapFactory
                .decodeResource(context?.resources, R.drawable.ic_marker)
        )
        // marker?.remove()//borra el marcardor si existe
        marker = mMap.addMarker(
            MarkerOptions()
                .position(loc) // posicion
                .title(site.name)
                .snippet(site.site)
                .icon(icon)
        )
    }

    /**
     * Metodo que muestra todos los lugares cercanos a la posicion dada por parametro
     * @param loc :LatLng
     */
    private fun addMarkerSite(loc: LatLng) {
        Log.i("mape", loc.latitude.toString() + "-" + loc.longitude.toString())
        var listSites = SiteController.selectByNear(loc.latitude, loc.longitude, DISTANCE)
        //si hay lugares los pinta
        if (listSites != null) {
            for (site in listSites) {
                Log.i("mape", site.toString())
                var loc = LatLng(site.latitude, site.longitude)
                markCurrentPostition(loc, site)
            }
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
                        addMarkerSite(posicion!!)
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

    private fun anadirLugares() {

        SiteController.deleteAllSite()
        //Ciudad Real
        val lugar = Site("Lugar 1", "ciudad", "fecha", 4.3, -3.940100, 38.981782)
        var lugar2 = Site(2, "Lugar 2", "ciudad", "fecha", 3.5, -3.945000, 38.981000)
        var lugar3 = Site(3, "Lugar 3", "fecha", "ciudad", 0.5, -3.940900, 38.981500)
        val lugar7 = Site(7, "Lugar 1", "fecha", "ciudad", 4.3, -3.918120, 38.980935)
        var lugar8 = Site(8, "Lugar 2", "fecha", "ciudad", 3.5, -3.940031, 38.993067)
        var lugar9 = Site(9, "Lugar 3", "fecha", "ciudad", 0.5, -3.942874, 38.971091)
        //Puertollano
        val lugar4 = Site(10, "Lugar 1", "fecha", "ciudad", 4.3, -4.11179038, 38.707595)
        var lugar5 = Site(11, "Lugar 2", "fecha", "ciudad", 3.5, -4.11017800, 38.702733)
        var lugar6 = Site(12, "Lugar 3", "fecha", "ciudad", 0.5, -4.08364100, 38.682322)

        SiteController.insertSite(lugar)
        SiteController.insertSite(lugar2)
        SiteController.insertSite(lugar3)
        SiteController.insertSite(lugar4)
        SiteController.insertSite(lugar5)
        SiteController.insertSite(lugar6)
        SiteController.insertSite(lugar7)
        SiteController.insertSite(lugar8)
        SiteController.insertSite(lugar9)
    }

}
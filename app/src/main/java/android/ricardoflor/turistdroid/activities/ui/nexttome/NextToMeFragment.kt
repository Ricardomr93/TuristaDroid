package android.ricardoflor.turistdroid.activities.ui.nexttome

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.ricardoflor.turistdroid.MyApplication
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.NavigationActivity
import android.ricardoflor.turistdroid.activities.ui.mySites.MySitesFragment
import android.ricardoflor.turistdroid.activities.ui.mySites.SiteFragment
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
import android.ricardoflor.turistdroid.bd.site.SiteDTO
import android.ricardoflor.turistdroid.bd.site.SiteMapper
import android.ricardoflor.turistdroid.bd.user.UserMapper
import android.ricardoflor.turistdroid.utils.UtilImage
import android.ricardoflor.turistdroid.utils.UtilSession
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class NextToMeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Variables a usar y permisos del mapa
    private lateinit var mMap: GoogleMap
    private lateinit var myPosition: FusedLocationProviderClient
    private var location: Location? = null
    private var posicion: LatLng? = null
    private var locationRequest: LocationRequest? = null
    private var DISTANCE = 0.035000


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
        //getPosition()
        addMarkerSite()
        locationReq()
        clickOnInfoWIndow()
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
        //bc.include(LatLng(posicion!!.latitude, posicion!!.longitude))
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

    /**
     * Metodo que muestra todos los lugares cercanos a la posicion dada por parametro
     * @param loc :LatLng
     */
    private fun addMarkerSite() {
        val turistREST = TuristAPI.service
        val call = turistREST.siteGetAll()
        call.enqueue((object : Callback<List<SiteDTO>> {
            override fun onResponse(call: Call<List<SiteDTO>>, response: Response<List<SiteDTO>>) {
                Log.i("REST", "Entra en onResponse addMarkerSite")
                if (response.isSuccessful && response.body()!!.isNotEmpty()) {
                    Log.i("REST", "Entra en isSuccessful addMarkerSite")
                    val siteList =
                        SiteMapper.fromDTO(response.body() as MutableList<SiteDTO>) as MutableList<Site>//saca todos los resultados
                    //Los va recorriendo y rellenando
                    for (site in siteList) {
                        Log.i("mape", site.toString())
                        val loc = LatLng(site.latitude, site.longitude)
                        markCurrentPostition(loc, site)
                    }
                    allSeeMarker(siteList)
                }
            }
            override fun onFailure(call: Call<List<SiteDTO>>, t: Throwable) {
                Log.i("Rest","Entra en onFailure addMarkerSite")
            }

        }))
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
                ratin.text = site.rating.toString()
                return row
            }
        })
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
                        //addMarkerSite(posicion!!)
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
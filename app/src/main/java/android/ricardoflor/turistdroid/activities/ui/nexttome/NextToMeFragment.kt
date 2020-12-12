package android.ricardoflor.turistdroid.activities.ui.nexttome

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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
    private var marcadorTouch: Marker? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null

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

    private fun initMap() {
        val mapFragment = (childFragmentManager
            .findFragmentById(R.id.mapNextToMe) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(maps: GoogleMap) {
        mMap = maps
        mMap.isMyLocationEnabled = true
        configurarIUMapa()
        obtenerPosicion()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.i("mape", marker.toString())
        Toast.makeText(
             context, marker.title.toString() +
                       " Mal sitio para ir.",
               Toast.LENGTH_SHORT
            ).show()
        return false
    }

    /**
     * Configuración por defecto del modo de mapa
     */
    private fun configurarIUMapa() {
        Log.i("mape", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isMapToolbarEnabled = true
        mMap.setMinZoomPreference(15.0f)
    }

    private fun myActualPosition() {
        myPosition = LocationServices.getFusedLocationProviderClient(activity!!)
    }

    /**
     * Obtiene la posición
     */
    private fun obtenerPosicion() {
        Log.i("Mape", "Opteniendo posición")
        try {
            // Lo lanzamos como tarea concurrente
            val local: Task<Location> = myPosition!!.lastLocation
            local.addOnCompleteListener(
                activity!!
            ) { task ->
                if (task.isSuccessful) {
                    // Actualizamos la última posición conocida
                    //try {
                    localizacion = task.result
                    posicion = LatLng(
                        localizacion!!.latitude,
                        localizacion!!.longitude
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                } else {
                    Log.i("GPS", "No se encuetra la última posición.")
                    Log.e("GPS", "Exception: %s", task.exception)
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                view!!,
                "No se ha encontrado su posoción actual o el GPS está desactivado",
                Snackbar.LENGTH_LONG
            ).show();
            Log.e("Exception: %s", e.message.toString())
        }
    }
}
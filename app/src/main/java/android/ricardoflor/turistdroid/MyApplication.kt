package android.ricardoflor.turistdroid

import android.Manifest
import android.app.Application
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.session.Session
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserController
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MyApplication : Application() {
    //Variable estatica

    companion object {
        var USER = User()
    }
    var PERMISSIONSCAMERA = false
    var PERMISSIONSGALLERY = false
    var PERMISSIONSLOCATION = false

    // Cloud Firestore
    private lateinit var db: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        BdController.initRealm(this)
        sessionExist()
        initAllPermisses()

        db = Firebase.firestore

    }

    fun sessionExist() {
        /*try {
            SESSION = SessionController.selectSession()!!
            if (SESSION.userId != "") {
                USER = UserController.selectByEmail(SESSION.useremail)!!
                Log.i("util", "Usuario existe $USER")
            } else {
                Log.i("util", "otro")
            }
        } catch (ex: IllegalArgumentException) {
        }*/

    }

    //************************************************************
    //METODOS PERMISOS********************************************
    /**
     * Comprobamos los permisos de la aplicación
     */
    fun initPermissesGallery():Boolean {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(this)
            //PERMISOS
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.i("util", "todos permisos galeria")
                        PERMISSIONSGALLERY = true
                    }
                    // COMPROBAMOS QUE NO HAY PERMISOS SIN ACEPTAR
                    if (report.isAnyPermissionPermanentlyDenied) {
                        PERMISSIONSGALLERY = false
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
                    this?.applicationContext,
                    getString(R.string.need_gallery),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
        return PERMISSIONSGALLERY
    }

    /**
     * Comprobamos los permisos de la aplicación
     */
    fun initPermissesLocation() :Boolean {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(this)
            //PERMISOS
            .withPermissions(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.i("util", "todos permisos localizacion")
                        PERMISSIONSLOCATION = true
                    }
                    // COMPROBAMOS QUE NO HAY PERMISOS SIN ACEPTAR
                    if (report.isAnyPermissionPermanentlyDenied) {
                        PERMISSIONSLOCATION = false
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
                    this?.applicationContext,
                    getString(R.string.need_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
        return PERMISSIONSLOCATION
    }

    /**
     * Comprobamos los permisos de la aplicación
     */
    fun initPermissesCamera(): Boolean {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(this)
            //PERMISOS
            .withPermissions(
                Manifest.permission.CAMERA,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.i("util", "todos permisos camara")
                         PERMISSIONSCAMERA = true
                    }
                    // COMPROBAMOS QUE NO HAY PERMISOS SIN ACEPTAR
                    if (report.isAnyPermissionPermanentlyDenied) {
                        PERMISSIONSCAMERA = false
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
                    this?.applicationContext,
                    getString(R.string.need_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
        return PERMISSIONSCAMERA
    }

    private fun initAllPermisses() {
        Dexter.withContext(this)
            //PERMISOS
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.i("util", "todos permisos")
                        PERMISSIONSCAMERA = true
                        PERMISSIONSGALLERY = true
                        PERMISSIONSLOCATION = true
                    }
                }//NOTIFICAR DE LOS PERMISOS

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
    }
}
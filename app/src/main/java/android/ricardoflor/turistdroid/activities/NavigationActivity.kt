package android.ricardoflor.turistdroid.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.user.UserDTO
import android.ricardoflor.turistdroid.bd.user.UserMapper
import android.ricardoflor.turistdroid.utils.RoundImagePicasso
import android.ricardoflor.turistdroid.utils.UtilImage
import android.ricardoflor.turistdroid.utils.UtilExp
import android.ricardoflor.turistdroid.utils.UtilSession
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.nav_header_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NavigationActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    public var isEventoFila = true

    //LINTERNA
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var encendida: Boolean = false
    //tipo de proveedor
    enum class ProviderType {
        BASIC,
        GOOGLE
    }

    //autenticador
    private lateinit var auth: FirebaseAuth
    // Cloud Firestore
    private lateinit var db: FirebaseFirestore

    companion object {
        lateinit var navUsername: TextView
        lateinit var navUserEmail: TextView
        lateinit var navUserImage: ImageView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        /* TODO -> esta es la unica forma que he encontrado para que no falle la aplicacion al girar en SiteFragment
           Lo que pasa es que gira la pantalla y vuelve a MySitesFragment, el fragment anterior al que me encuentro */

        super.onCreate(null)
        //super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_my_sites, R.id.nav_next_to_me, R.id.nav_my_profile, R.id.nav_logout, R.id.nav_site
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //opciones adicionales
        //actualizarDatos(this)
        init()
        cambiarDatos()

    }
    private fun init(){
        auth = Firebase.auth
        val bundle = intent.extras
        if (bundle != null){
            val provider = bundle?.getString("provider")
            val prefs = getSharedPreferences("TuristDroid",Context.MODE_PRIVATE).edit()
            prefs.putString("provider",provider)
            prefs.apply()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navigation, menu)
        initLight()
        return true
    }

    /**
     * Funcion para manejar los eventos clic del menu derecho (tres puntitos)
     *
     * @param item
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_export -> {
                // EXPORTAR
                UtilExp.export(this)
                true
            }

            R.id.action_light -> {
                if (encendida) {
                    //Log.i("light", "Linterna OFF")
                    item.setIcon(R.drawable.ic_ricflor_lantern_white_off)
                    encendida = false
                    turnOnLight()

                } else {
                    //Log.i("light", "Linterna ON")
                    item.setIcon(R.drawable.ic_ricflor_lantern_white_on)
                    encendida = true
                    turnOnLight()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        isEventoFila = true
        super.onBackPressed()
    }

    /**
     * Funcion para arrancar la linterna
     */
    private fun initLight() {
        val isFlashAvailable =
            applicationContext?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        if (!isFlashAvailable!!) {
            val alert = AlertDialog.Builder(applicationContext)
                .create()
            alert.setTitle(R.string.error)
            alert.setMessage(R.string.error_no_flash.toString())
            alert.setButton(DialogInterface.BUTTON_POSITIVE, R.string.ok.toString()) { _, _ ->
                Log.i("Linterna", "Sin linterna")
            }
            //alert.show()
        }
        cameraManager = applicationContext?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Metodo que enciende la linterna
     */
    private fun turnOnLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, encendida)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()

        } catch (e: Exception) {
            Log.i("util", "Se ha producido un error con la linterna: " + e.printStackTrace())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    /**
     * Metodo que recoge los datos del user y los almacena en los datos del nav
     */
    private fun cambiarDatos(){
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        navUsername = headerView.findViewById(R.id.txtNavUser)
        navUserEmail = headerView.findViewById(R.id.txtNavEmail)
        navUserImage = headerView.findViewById(R.id.imgNavUser)
        //obtenemos el email de la sesion y obtenemos el usuario
        Log.i("util", USER.toString())
        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl
            Log.i("fairebase","photoUrl: $photoUrl")
            val uid = user.uid
            //cambiamos los valores por los del usuario
            navUsername.text = name
            navUserEmail.text = email
            Picasso.get()
                .load(photoUrl)
                .transform(RoundImagePicasso())
                .into(navUserImage)
        }
    }
}
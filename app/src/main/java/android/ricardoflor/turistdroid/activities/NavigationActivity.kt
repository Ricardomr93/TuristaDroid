package android.ricardoflor.turistdroid.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.ui.mySites.MySitesFragment
import android.ricardoflor.turistdroid.activities.ui.myprofile.MyProfileFragment
import android.ricardoflor.turistdroid.activities.ui.nexttome.NextToMeFragment
import android.ricardoflor.turistdroid.activities.LoginActivity.Companion.USER
import android.ricardoflor.turistdroid.utils.UtilImage
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
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_navigation.*


class NavigationActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    public var isClicEventoFila = true

    //LINTERNA
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var encendida: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                R.id.nav_my_sites, R.id.nav_next_to_me, R.id.nav_my_profile, R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navigationListener(navView)

        //opciones adicionales
        getInformation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navigation, menu)
        initLight()
        return true
    }

    /**
     * Funcion para manejar los eventos clic del menu izquierdo (tres rayas)
     *
     * @param navigationView
     */
    private fun navigationListener(navigationView: NavigationView){
        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_my_sites -> {
                    openMySites()
                    // Para abrir y cerrar el menu izquierdo
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_next_to_me -> {
                    openNextToMe()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_my_profile -> {
                    openMyProfile()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                R.id.nav_logout -> {
                    // Log.i("navig", "exit")
                    logout()
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    true
                }
                else -> {
                    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }
                    false
                }
            }
        }
    }

    /**
     * Funcion para manejar los eventos clic del menu derecho (tres puntitos)
     *
     * @param item
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_import -> {
                // IMPORTAR
                true
            }

            R.id.action_export -> {
                // EXPORTAR
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

    private fun turnOnLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, encendida)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Funcion para abrir el fragment My Sites
     */
    private fun openMySites(){
        val newFragment = MySitesFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Funcion para abrir el fragment Next To Me
     */
    private fun openNextToMe(){
        val newFragment = NextToMeFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Funcion para abrir el fragment My Profile
     */
    private fun openMyProfile(){
        val newFragment = MyProfileFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Funcion para Cerrar Sesion
     */
    private fun logout() {
        UtilSession.deleteSession()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun getInformation() {

        // actualizamos el perfil con los datos de la sesion
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.txtNavUser)
        val navUserEmail: TextView = headerView.findViewById(R.id.txtNavEmail)
        val navUserImage: ImageView = headerView.findViewById(R.id.imgNavUser)

        //obtenemos el email de la sesion y obtenemos el usuario
        Log.i("util", USER.toString())
        //cambiamos los valores por los del usuario
        navUsername.text = USER.nameUser
        navUserEmail.text = USER.email
        if (USER.image != "") {
            Log.i("util", "Carga imagen")
            navUserImage.setImageBitmap(UtilImage.toBitmap(USER.image))
            UtilImage.redondearFoto(navUserImage)
        }
    }

}
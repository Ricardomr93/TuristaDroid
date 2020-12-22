package android.ricardoflor.turistdroid.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilImage
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_singin.*
import java.io.IOException

class SinginActivity : AppCompatActivity() {

    private var name = ""
    private var nameuser = ""
    private var email = ""
    private var pass = ""
    private var user = User()
    private lateinit var FOTO: Bitmap
    private lateinit var IMAGE: Uri
    private var image : Bitmap? = null
    // Constantes
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singin)
        initUI()
    }
    /**
     * Metodo para registrar un usuario
     * Una vez registrado, vuelve al LoginActivity
     */
    fun singin() {
        btnSing.setOnClickListener {
            if (anyEmpty()) {
                try {
                    if (isMailValid(txtEmail.text.toString())) {//campo email correcto
                        //Comprobar el campo password
                            addUser()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                    } else {
                        txtEmail.error = resources.getString(R.string.email_incorrecto)
                    }
                } catch (ex: RealmPrimaryKeyConstraintException) {
                    txtEmail.error = resources.getString(R.string.isAlreadyExist)
                }

            }
        }
    }

    /**
     * Metodo que coge los datos de los txt y los almacena a un usuario y lo inserta en la base de datos
     */
    private fun addUser() {
        user.name = txtName.text.toString()
        user.password = UtilEncryptor.encrypt(txtPass.text.toString())!!
        user.nameUser = txtUserName.text.toString()
        user.email = txtEmail.text.toString()
        if (this::FOTO.isInitialized) {
            user.image = UtilImage.toBase64(FOTO)!!
        }
        UserController.insertUser(user)
        USER = user
        Log.i("user", user.toString())
    }

    /**
     * Metodo para validar el EMAIL
     */
    private fun isMailValid(mail: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(mail).matches()
    }

    /**
     * Metodo para validar la PASSWORD
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
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
     * Método que devuelve false si alguno de los valores está vácio
     */
    private fun anyEmpty(): Boolean {
        var valid = true
        if (notEmpty(txtName) && notEmpty(txtEmail) && notEmpty(txtPass) && notEmpty(txtUserName)) {
            valid = false
        }
        return valid
    }
    //************************************************************
    //METODOS PARA LA IMAGEN**************************************

    /**
     * Inicia la interfaz y los eventos de la apliación
     */
    private fun initUI() {
        initButtoms()
        initPermisses()
        singin()
    }

    /**
     * Inicia los eventos de los botones
     */
    private fun initButtoms() {
        imgBtnPhoto.setOnClickListener {
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
        AlertDialog.Builder(this)
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
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //CAPTURA LA FOTO
        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE)
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
        //Si cancela no hace nada
        if (resultCode == RESULT_CANCELED) {
            Log.d("sing", "Se ha cancelado")
        }
        //si elige la opcion de galeria entra en la galeria
        if (requestCode == GALLERY) {
            Log.d("sing", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI
                val contentURI = data.data!!
                try {
                    FOTO = differentVersion(contentURI)
                    imgBtnPhoto.setImageBitmap(FOTO)//mostramos la imagen
                    UtilImage.redondearFoto(imgBtnPhoto)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, getText(R.string.error_gallery), Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            Log.d("sing", "Entramos en Camara")
            //cogemos la imagen
            try {
                FOTO = differentVersion(IMAGE)
                // Mostramos la imagen
                imgBtnPhoto.setImageBitmap(FOTO)
                UtilImage.redondearFoto(imgBtnPhoto)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, getText(R.string.error_camera), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Metodo que devueleve un bitmap dependiendo de la version
     * @return un bitmap
     */
    fun differentVersion(contentURI: Uri): Bitmap {
        //Para controlar la version de android usar uno u otro
        val bitmap: Bitmap
        bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, contentURI);
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap;
    }
    //************************************************************
    //METODO  PARA LOS PERMISOS**********************
    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisses() {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(this)
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
                    this,
                    getString(R.string.error_permissions),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()

    }
    //************************************************************
    //METODOS PARA LA RECUPERACION DE DATOS***********************
    /**
     * Método sobreescrito que salva el estado en el ciclo del vida
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // Salvamos en un bundle estas variables o estados de la interfaz
        outState.run {
            // Actualizamos los datos o los recogemos de la interfaz
            putString("EMAIL", email)
            putString("NAME", name)
            putString("NAMEUSER", nameuser)
            putString("PASSWORD", pass)
            putString("IMAGE", image?.let { UtilImage.toBase64(it) })
        }
        // llama a la clase padre para salvar los datos
        super.onSaveInstanceState(outState)
    }
    /**
     * Metodo sobreescrito para recuperar el estado del ciclo de vida
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Recuperamos las variables y los estados
        super.onRestoreInstanceState(savedInstanceState)
        // Recuperamos del Bundle
        savedInstanceState.run {
            name = getString("NAME").toString()
            email = getString("EMAIL").toString()
            nameuser = getString("NAMEUSER").toString()
            pass = getString("PASSWORD").toString()
            image = UtilImage.toBitmap(getString("IMAGE").toString())
        }
    }
}
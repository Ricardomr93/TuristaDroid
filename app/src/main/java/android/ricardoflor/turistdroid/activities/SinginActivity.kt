package android.ricardoflor.turistdroid.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.bd.User
import android.ricardoflor.turistdroid.bd.UserController
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilImage
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
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
    private var user = User();
    private var image: Bitmap? = null

    // Constantes
    private val GALERIA = 1
    private val CAMARA = 2

    // https://developer.android.com/training/data-storage/shared/media?hl=es-419
    private val IMAGEN_DIR = "/TodoCamara2020"
    private lateinit var IMAGEN_URI: Uri
    private val PROPORCION = 600
    private var IMAGEN_NOMBRE = ""
    private var IMAGEN_COMPRES = 30


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singin)
        singin()
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
                    //Comprobar el campo password
                    if (isPasswordValid(txtPass.text.toString())) {
                        addUser()
                    } else {
                        txtPass.error = resources.getString(R.string.pwd_incorrecto)
                    }

                } catch (ex: RealmPrimaryKeyConstraintException) {
                    txtEmail.error = resources.getString(R.string.isAlreadyExist)
                }

            }
        }
    }
    private fun addUser() {
        user.name = txtName.text.toString()
        user.password = UtilEncryptor.encrypt(txtPass.text.toString())!!
        user.nameUser = txtUserName.text.toString()
        user.email = txtEmail.text.toString()
        user.image = UtilImage.toBase64(imgBtnPhoto.drawable.toBitmap()).toString()
        UserController.insertUser(user)
        Log.i("user", user.toString())
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * Metodo para validar el EMAIL
     */
    private fun isMailValid(mail: String): Boolean {
        return if (mail.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(mail).matches()
        } else {
            mail.isNotBlank()
        }
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
    //************************************************************
    //METODOS PARA LA IMAGEN**************************************

    /**
     * Inicia la interfaz y los eventos de la apliación
     */
    private fun initUI() {

        // Eventos botones
        initBotones()

        // Iniciamos los permisos
        initPermisos()
    }
    /**
     * Inicia los eventos de los botones
     */
    private fun initBotones() {
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
        // Creamos el dialog con su builder
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.SelectOption))
            .setItems(fotoDialogoItems) { dialog, modo ->
                when (modo) {
                    0 -> elegirFotoGaleria()
                    1 -> tomarFotoCamara()
                }
            }
            .show()
    }

    /**
     * Elige una foto de la galeria
     */
    private fun elegirFotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALERIA)
    }

    //Llamamos al intent de la camara
    private fun tomarFotoCamara() {

        // Si queremos hacer uso de fotos en alta calidad
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Eso para alta o baja
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = UtilImage.crearNombreFichero()
        // Salvamos el fichero
        val fichero = UtilImage.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, applicationContext)!!
        IMAGEN_URI = Uri.fromFile(fichero)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGEN_URI)
        // Esto para alta y baja
        startActivityForResult(intent, CAMARA)
    }

    /**
     * Siempre se ejecuta al realizar una acción
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("sing", "Opción::--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            Log.d("sing", "Se ha cancelado")
        }
        if (requestCode == GALERIA) {
            Log.d("sing", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                val contentURI = data.data!!
                try {
                    // Obtenemos el bitmap de su almacenamiento externo
                    // Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    val bitmap: Bitmap
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentURI);
                    } else {
                        val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI)
                        bitmap = ImageDecoder.decodeBitmap(source)
                    }
                    // Para jugar con las proporciones y ahorrar en memoria no cargando toda la foto, solo carga 600px max
                    val prop = PROPORCION / bitmap.width.toFloat()
                    // Actualizamos el bitmap para ese tamaño, luego podríamos reducir su calidad
                    val foto = Bitmap.createScaledBitmap(bitmap, PROPORCION, (bitmap.height * prop).toInt(), false)
                    Toast.makeText(this, "¡Foto rescatada de la galería!", Toast.LENGTH_SHORT).show()
                    imgBtnPhoto.setImageBitmap(bitmap)
                    // Vamos a compiar nuestra imagen en nuestro directorio
                    UtilImage.copiarImagen(bitmap, IMAGEN_DIR, IMAGEN_COMPRES, applicationContext)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMARA) {
            Log.d("sing", "Entramos en Camara")
            // Cogemos la imagen, pero podemos coger la imagen o su modo en baja calidad (thumbnail)
            try {
                val foto: Bitmap
                //Para controlar la version de android usar uno u otro
                if (Build.VERSION.SDK_INT < 28) {
                    foto = MediaStore.Images.Media.getBitmap(contentResolver, IMAGEN_URI)
                } else {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, IMAGEN_URI)
                    foto = ImageDecoder.decodeBitmap(source)
                }
                // Mostramos
                imgBtnPhoto.setImageBitmap(foto)
                //mainTvPath.text = IMAGEN_URI.toString()
                Log.i("sing","Foto guardada")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("sing","Fallo con la camara")
            }
        }
    }
    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisos() {
        // Indicamos el permisos y el manejador de eventos de los mismos
        Dexter.withContext(this)
            // Lista de permisos a comprobar
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            // Listener a ejecutar
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // ccomprbamos si tenemos los permisos de todos ellos
                    if (report.areAllPermissionsGranted()) {
                        Log.i("sing","¡Todos los permisos concedidos!")
                    }

                    // comprobamos si hay un permiso que no tenemos concedido ya sea temporal o permanentemente
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // abrimos un diálogo a los permisos
                        //openSettingsDialog();
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { Toast.makeText(applicationContext, "Existe errores! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }
}
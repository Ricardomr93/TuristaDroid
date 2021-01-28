package android.ricardoflor.turistdroid.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.ricardoflor.turistdroid.MyApplication
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserDTO
import android.ricardoflor.turistdroid.bd.user.UserMapper
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilImage
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_singin.*
import java.io.IOException
import java.lang.NullPointerException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SinginActivity : AppCompatActivity() {

    private var name = ""
    private var nameuser = ""
    private var email = ""
    private var pass = ""
    private var user: User? = null
    private lateinit var FOTO: Bitmap
    private var IMAGE: Uri? = null
    private var image: Bitmap? = null

    // Constantes
    private val IMAGEN_DIR = "/TuristDroid"
    private lateinit var IMAGEN_NOMBRE: String
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
     * Método que haceun intent al login
     */
    fun startLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * Metodo que coge los datos de los txt y los almacena a un usuario y lo inserta en la base de datos
     */
    private fun addUser() {
        var im = ""
        if (this::FOTO.isInitialized) {
            im = UtilImage.toBase64(FOTO)!!
        }
            user = User(
                name = txtName.text.toString(),
                nameUser = txtUserName.text.toString(),
                password = UtilEncryptor.encrypt(txtPass.text.toString())!!,
                email = txtEmail.text.toString(),
                image = im ,
                twitter = "",
                instagram = "",
                facebook = "",
            )

        val turistREST = TuristAPI.service
        val call: Call<UserDTO> = turistREST.userPost(UserMapper.toDTO(user!!))
        call.enqueue(object : Callback<UserDTO> {
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                if (response.isSuccessful) {
                    startLogin()
                } else {
                    Toast.makeText(applicationContext, "Error POST", Toast.LENGTH_SHORT).show() //TODO -> cambiar texto
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                Toast.makeText(applicationContext,
                    getText(R.string.service_error).toString() + t.localizedMessage,
                    Toast.LENGTH_LONG)
                    .show()
            }

        })
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
                    0 -> {
                        if ((this.application as MyApplication).initPermissesGallery()) {
                            takephotoFromGallery()
                        } else {
                            (this.application as MyApplication).initPermissesGallery()
                        }
                    }
                    1 -> {
                        if ((this.application as MyApplication).initPermissesCamera()) {
                            takePhotoFromCamera()
                        } else {
                            (this.application as MyApplication).initPermissesCamera()
                        }
                    }
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
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = UtilImage.crearNombreFichero()
        // guardamos el fichero en una variable
        val file = UtilImage.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, this)
        IMAGE = Uri.fromFile(file)
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
                FOTO = differentVersion(IMAGE!!)
                // Mostramos la imagen
                imgBtnPhoto.setImageBitmap(FOTO)
                UtilImage.redondearFoto(imgBtnPhoto)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (ex: Exception) {
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
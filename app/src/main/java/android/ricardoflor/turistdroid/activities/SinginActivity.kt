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
import android.provider.Settings
import android.ricardoflor.turistdroid.MyApplication
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilImage
import android.ricardoflor.turistdroid.utils.UtilNet
import android.ricardoflor.turistdroid.utils.UtilText
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_singin.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.time.Instant


class SinginActivity : AppCompatActivity() {

    private var name = ""
    private var nameuser = ""
    private var email = ""
    private var pass = ""
    private lateinit var FOTO: Bitmap
    private var IMAGE: Uri? = null
    private var image: Bitmap? = null

    //autenticador
    private lateinit var auth: FirebaseAuth

    //storage
    lateinit var storage: FirebaseStorage


    private lateinit var IMAGEN_NOMBRE: String
    private lateinit var txtname: String
    private lateinit var txtxnameUser: String
    private lateinit var txtpassword: String
    private lateinit var txtemail: String


    // Constantes
    private val IMAGEN_DIR = "/TuristDroid"
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singin)
        initUI()
        txtxnameUser = txtUserName.text.toString()
        txtpassword = UtilEncryptor.encrypt(txtPass.text.toString())!!
        txtemail = txtEmail.text.toString()
    }

    /**
     * Metodo para registrar un usuario
     * Una vez registrado, vuelve al LoginActivity
     */
    fun singin() {
        btnSing.setOnClickListener {
            UtilText.cleanErrors(txtInLaSingEmail, txtInLaSingName, txtInLaSingPass)
            createAccount()
        }
    }

    /**
     * Método que hace un intent al login
     */
    fun startLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * Método que devuelve false si alguno de los valores está vácio
     */
    private fun anyEmpty(): Boolean {
        var valid = true
        if (UtilText.empty(txtEmail, txtInLaSingEmail, this) || UtilText.empty(txtPass, txtInLaSingPass, this)
            || UtilText.empty(txtUserName, txtInLaSingName, this)
        ) {
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
        storage = Firebase.storage
        auth = Firebase.auth
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
                FOTO = Bitmap.createScaledBitmap(FOTO, 100 /*Ancho*/, 100 /*Alto*/, false /* filter*/)

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

    private fun isCorrect(txtemail: String): Boolean {
        var valide = false
        if (anyEmpty() && UtilText.isMailValid(txtemail)) {
            if (UtilNet.hasInternetConnection(this)) {
                valide = true
            } else {
                val snackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.no_net,
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setActionTextColor(getColor(R.color.accent))
                snackbar.setAction("Conectar") {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                    finish()
                }
                snackbar.show()
            }
        } else {
            txtInLaSingEmail.error = resources.getString(R.string.email_incorrecto)
        }
        return valide
    }

    private fun createAccount() {
        txtpassword = UtilEncryptor.encrypt(txtPass.text.toString())!!
        txtemail = txtEmail.text.toString()
        if (!isCorrect(txtemail)) {
            return
        }
        if (!UtilText.isPasswordValid(txtPass.text.toString())) {
            txtInLaSingPass.error = resources.getString(R.string.pwd_incorrecto)
            return
        }
        Log.d("fairbase", "createAccount:$txtemail")
        // empieza la creacion del usuario con el email
        auth.createUserWithEmailAndPassword(txtemail, txtpassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("fairbase", "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateProfile(user!!)
                    startLogin()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("fairbase", "createUserWithEmail:failure", task.exception)
                    txtInLaSingEmail.error = resources.getString(R.string.isAlreadyExist)
                }
                // [START_EXCLUDE]
                //puede que lo haga
                //hideProgressBar()
                // [END_EXCLUDE]
            }
    }

    private fun updateProfile(user: FirebaseUser) {

        val profileUpdates = userProfileChangeRequest {
            displayName = txtUserName.text.toString()
            loadImage(displayName!!, user)
        }
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "User profile updated.")
                }
            }
        // [END update_profile]
    }

    private fun loadImage(string: String, user: FirebaseUser) {
        if (!this::FOTO.isInitialized) {
            return
        }
        val time = Instant.now().toString()
        val nombre = "$string$time"
        val baos = ByteArrayOutputStream()
        FOTO.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val imageRef = storage.reference.child("images/$nombre.jpg")
        var file = Uri.fromFile(File("path/to/images/$nombre.jpg"))
        var uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener {
            Log.i("fairebase", "error al subir la foto a storage")
        }.addOnSuccessListener { taskSnapshot ->
            uploadTask = imageRef.putFile(file)
            val urlTask = uploadTask.continueWithTask { task ->
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileUpdates = userProfileChangeRequest {
                        val uri = task.result
                        photoUri = uri
                        Log.i("fairebase", "uri: $uri")
                    }
                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("TAG", "uri profile good")
                            }
                        }
                }
            }
        }
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
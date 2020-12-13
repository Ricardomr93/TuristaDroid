package android.ricardoflor.turistdroid.activities.ui.myprofile

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.ricardoflor.turistdroid.MyApplication.Companion.SESSION
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.activities.NavigationActivity
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilImage
import android.ricardoflor.turistdroid.utils.UtilSession
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.fragment_my_profile.*
import java.io.IOException

class MyProfileFragment : Fragment() {
    // Constantes
    private val GALLERY = 1
    private val CAMERA = 2
    lateinit var IMAGE: Uri
    private val IMAGEN_DIR = "/TuristDroid"
    private lateinit var FOTO: Bitmap
    private lateinit var IMAGEN_NOMBRE: String
    private var user = User()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        updateUser()
        deleteUser()
        getInformation()
        initButtoms()
        initPermisses()
    }

    /**
     * Metodo que devuelve un boolean si el email cambia o no
     * @return true si cambia false si no
     */
    fun emailChanges(): Boolean {
        return USER.email != txtEmailMyProfile.text.toString()
    }

    /**
     * Metodo que coge los datos de los txt y los almacena a un usuario y lo inserta en la base de datos
     */
    private fun update() {
        val name = txtNameMyProfile.text.toString()
        val nameUser = txtUserNameMyProfile.text.toString()
        val email = txtEmailMyProfile.text.toString()
        Log.d("profile", name + nameUser + email)
        user.name = name
        user.password = UtilEncryptor.encrypt(txtPassMyprofile.text.toString())!!
        user.nameUser = nameUser
        user.email = email
        if (this::FOTO.isInitialized) {
            user.image = UtilImage.toBase64(FOTO)!!
        } else {
            user.image = USER.image
        }
        UserController.updateUser(user)
    }

    /**
     * Metodo que modifica al usuario si los campos son correctos
     */
    private fun updateUser() {
        btnUpdateMyprofile.setOnClickListener {
            if (isMailValid(txtEmailMyProfile.text.toString())) {
                if (isPasswordValid(txtPassMyprofile.text.toString())) {
                    if (!someIsEmpty()) {
                        dialogUpdate()
                    }
                } else {
                    txtPassMyprofile.error = resources.getString(R.string.pwd_incorrecto)
                }
            } else {
                txtEmailMyProfile.error = resources.getString(R.string.email_incorrecto)
            }
        }
    }

    /**
     * Método que cambia los datos del usuario en el navigation
     */
    fun changeNavigation() {
        NavigationActivity.navUsername.text = USER.nameUser
        NavigationActivity.navUserEmail.text = USER.email
        if (USER.image != "") {
            Log.i("util", "Carga imagen")
            NavigationActivity.navUserImage.setImageBitmap(UtilImage.toBitmap(USER.image))
            UtilImage.redondearFoto(NavigationActivity.navUserImage)
        }
    }

    /**
     * Metodo que borra e inserta
     */
    fun deleteAndInsertUser() {
        UserController.deleteUser(USER.email)
        UtilSession.closeSession()
        addUser()
        SESSION = SessionController.selectSession()!!
    }


    /**
     * Metodo que coge los datos de los txt y los almacena a un usuario y lo inserta en la base de datos
     */
    private fun addUser() {
        try {
            user.name = txtNameMyProfile.text.toString()
            //si la contraseña está vacia mantiene la que ya tiene
            if (txtPassMyprofile.text.isNullOrEmpty()) {
                user.password = USER.password
            } else {
                user.password = UtilEncryptor.encrypt(txtPassMyprofile.text.toString())!!
            }
            user.nameUser = txtUserNameMyProfile.text.toString()
            user.email = txtEmailMyProfile.text.toString()
            if (this::FOTO.isInitialized) {
                user.image = UtilImage.toBase64(FOTO)!!
            } else {
                user.image = USER.image
            }
            UserController.insertUser(user)
            USER = user
            UtilSession.createSession(user.email)
            Log.i("user", user.toString())
        } catch (ex: RealmPrimaryKeyConstraintException) {
            txtEmailMyProfile.error = resources.getString(R.string.isAlreadyExist)
            restauredUser()//restaura el usuario para no perder los datos
        }
    }

    /**
     * Restaura los datos del usuario borrado por si ocurre algun error
     */
    fun restauredUser() {
        UserController.insertUser(USER)
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
        return password.length > 5 || password.isNullOrEmpty()
    }

    /**
     * Método que comprueba si el campo esta vacio y lanza un mensaje
     * @param txt TextView
     */
    private fun empty(txt: TextView): Boolean {
        var empty = false
        if (txt.text.isEmpty()) {
            txt.error = resources.getString(R.string.isEmpty)
            empty = true
        }
        return empty
    }

    /**
     * Método que devuelve true si alguno de los valores está vácio
     */
    private fun someIsEmpty(): Boolean {
        var valid = false
        if (empty(txtEmailMyProfile) || empty(txtNameMyProfile) || empty(txtUserNameMyProfile)) {
            valid = true
        }
        return valid
    }

    /**
     * Metodo que al pulsar pide un cuadro de dialogo para confirmar
     */
    private fun deleteUser() {
        btnUnsubMyProfile.setOnClickListener {
            dialogDelete()
        }
    }

    /**
     * Cuadro de dialogo para advertir al usuario si queire borrar su cuenta
     * si acepta borra al usuario
     */
    private fun dialogDelete() {
        AlertDialog.Builder(context)
            .setTitle(getText(R.string.caution))
            .setMessage(getText(R.string.sure_delete))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                UserController.deleteUser(USER.email)
                UtilSession.closeSession()
                startActivity(Intent(context, LoginActivity::class.java))
                Toast.makeText(context!!, getText(R.string.userDelete), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.Cancel), null)
            .show()
    }

    private fun dialogUpdate() {
        AlertDialog.Builder(context)
            .setTitle(getText(R.string.caution))
            .setMessage(getText(R.string.sure_update))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                Log.i("updater", "usuario cambia")
                deleteAndInsertUser()
                Log.i("updater", UserController.selectAllUser().toString())
                changeNavigation()
                Toast.makeText(context!!, getText(R.string.update_user), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.Cancel), null)
            .show()
    }

    /**
     * Coge la informacion del usuario y lo muestra en los cuadros de texto
     */
    private fun getInformation() {
        txtEmailMyProfile.setText(USER.email)
        txtNameMyProfile.setText(USER.name)
        txtUserNameMyProfile.setText(USER.nameUser)
        if (USER.image != "") {
            Log.i("util", "Carga imagen")
            imgMyprofile.setImageBitmap(UtilImage.toBitmap(USER.image))
            UtilImage.redondearFoto(imgMyprofile)
        }

    }
    //************************************************************
    //METODO  PARA LOS PERMISOS**********************
    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisses() {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(context)
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
                    context?.applicationContext,
                    getString(R.string.error_permissions),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()


    }
    //************************************************************
    //METODOS PARA LA IMAGEN**************************************
    /**
     * Inicia los eventos de los botones
     */
    private fun initButtoms() {
        imgMyprofile.setOnClickListener {
            initDialogPhoto()
        }
    }

    /**
     * Muestra el diálogo para tomar foto o elegir de la galería
     */
    private fun initDialogPhoto() {
        val fotoDialogoItems = arrayOf(
            getString(R.string.Gallery),
            getString(R.string.Photo)
        )
        //Dialogo para eligir opciones
        AlertDialog.Builder(context)
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
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = UtilImage.crearNombreFichero()
        // guardamos el fichero en una variable
        val fichero = UtilImage.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, context!!)
        IMAGE = Uri.fromFile(fichero)
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
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            Log.d("sing", "Se ha cancelado")
        }
        //si elige la opcion de galeria entra en la galeria
        if (requestCode == GALLERY) {
            Log.d("profile", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI
                val contentURI = data.data!!
                try {
                    FOTO = differentVersion(contentURI)
                    imgMyprofile.setImageBitmap(FOTO)//mostramos la imagen
                    UtilImage.redondearFoto(imgMyprofile)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context!!, getText(R.string.error_gallery), Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            Log.d("profile", "Entramos en Camara")
            //cogemos la imagen
            try {
                FOTO = differentVersion(IMAGE)
                // Mostramos la imagen
                imgMyprofile.setImageBitmap(FOTO)
                UtilImage.redondearFoto(imgMyprofile)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context!!, getText(R.string.error_camera), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Metodo que devuleve un bitmap depende de la version
     * @return un bitmap
     */
    fun differentVersion(contentURI: Uri): Bitmap {
        //Para controlar la version de android usar uno u otro
        val bitmap: Bitmap
        bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context?.contentResolver, contentURI);
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(context?.contentResolver!!, contentURI)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap;
    }
}


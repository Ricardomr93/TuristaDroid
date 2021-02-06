package android.ricardoflor.turistdroid.activities.ui.myprofile

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
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.activities.NavigationActivity
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.session.SessionController
import android.ricardoflor.turistdroid.bd.session.SessionDTO
import android.ricardoflor.turistdroid.bd.user.User
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.bd.user.UserDTO
import android.ricardoflor.turistdroid.bd.user.UserMapper
import android.ricardoflor.turistdroid.utils.*
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.NullPointerException

class MyProfileFragment : Fragment() {
    // Constantes
    private val GALLERY = 1
    private val CAMERA = 2
    lateinit var IMAGE: Uri
    private val IMAGEN_DIR = "/TuristDroid"
    private lateinit var FOTO: Bitmap
    private lateinit var IMAGEN_NOMBRE: String
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
        btnUpdateMyprofile.setOnClickListener {
            UtilText.cleanErrors(txtInLaMyprofileEmail, txtInLaMyprofileName, txtInLaMyprofilePass)
            buttomUpdate()
        }
        btnUnsubMyProfile.setOnClickListener {
            UtilText.cleanErrors(txtInLaMyprofileEmail, txtInLaMyprofileName, txtInLaMyprofilePass)
            buttomDelete()
        }
        getInformation()
        initButtoms()

    }

    /**
     * Metodo que al pulsar el boton de update  comprueba que hay conexion
     * en caso de no haberla salta un mensaje para activarlo
     * si la hay modifica el usuario
     */
    private fun buttomUpdate() {
        if (UtilNet.hasInternetConnection(context)) {
            updateUser()
        } else {
            val snackbar = Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                R.string.no_net,
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(activity!!.getColor(R.color.accent))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
                activity!!.finish()
            }
            snackbar.show()
        }
    }

    /**
     * Metodo que al pulsar el boton de delete comprueba que hay conexion
     * en caso de no haberla salta un mensaje para activarlo
     * si la hay borra el usuario
     */
    private fun buttomDelete() {
        if (UtilNet.hasInternetConnection(context)) {
            dialogDelete()
        } else {
            val snackbar = Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                R.string.no_net,
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(activity!!.getColor(R.color.accent))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
                activity!!.finish()
            }
            snackbar.show()
        }
    }

    /**
     * Funcion para compartir con Gmail
     */
    private fun shareGmail() {
        //val uri = getImageUri(requireContext(), qrShare)
        val intent = Intent().apply {
            Intent(Intent.ACTION_SENDTO)
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_mysite))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_mysite))
            //putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    /**
     * Funcion para compartir con Twitter, Facebook e Instagram
     */
    fun shareSite(str: String) {
        val msg: String = getString(R.string.share_mysite)
        //val uri = getImageUri(requireContext(), qrShare)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, msg)
            type = "text/plain"
            //putExtra(Intent.EXTRA_STREAM, uri)
            //type = "image/jpeg"
            setPackage(str)
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
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
        if (emailChanges()) {//si el email es distinto al del usuario comprueba si existe en la bd
            emailExists()
        } else {
            updateRest()
        }
    }

    /**
     * Metodo que cambia los valores del usuario por los de las cajas de texto y devuelve el usuario
     */
    private fun changeUser(): User {
        val name = txtUserNameMyProfile.text.toString()
        val email = txtEmailMyProfile.text.toString()
        var im: String
        var pass: String
        Log.d("profile", name + email)
        if (this::FOTO.isInitialized) {
            im = UtilImage.toBase64(FOTO)!!
        } else {
            im = USER.image
        }
        if (txtPassMyprofile.text.toString().isEmpty()) {
            pass = USER.password
        } else {
            pass = UtilEncryptor.encrypt(txtPassMyprofile.text.toString())!!
        }
        val user = User(
            id = USER.id,
            name = name,
            password = pass,
            email = email,
            image = im,
            twitter = "",
            instagram = "",
            facebook = "",
        )
        Log.i("Update", user.toString())
        return user
    }

    /**
     * Metodo que comprueba que el email no existe para evitar duplicados
     */
    private fun emailExists() {
        val turistREST = TuristAPI.service
        val email = txtEmailMyProfile.text.toString()
        val call = turistREST.userGetByEmail(email)
        Log.i("REST", "email: $email")
        call.enqueue((object : Callback<List<UserDTO>> {

            override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                Log.i("REST", "emailExists en onResponse")
                if (response.isSuccessful) {
                    Log.i("REST", "emailExists en isSuccessful")
                    if (response.body()!!.isEmpty()) {
                        updateRest()
                    } else {
                        txtEmailMyProfile.error = resources.getString(R.string.isAlreadyExist)
                    }
                }
            }

            override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                Log.i("REST", "emailExists onFailure")
                Toast.makeText(
                    context,
                    getText(R.string.service_error),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }))

    }

    /**
     * Metodo que hace una llamada y modifica el usuario
     */
    private fun updateRest() {
        val user = changeUser()
        val turistREST = TuristAPI.service
        val call: Call<UserDTO> = turistREST.userUpdate(user.id, UserMapper.toDTO(user))
        call.enqueue(object : Callback<UserDTO> {
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                if (response.isSuccessful) {
                    USER = UserMapper.fromDTO(response.body()!!)
                    changeNavigation()
                    Toast.makeText(context!!, getText(R.string.update_user), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.error_put, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                Toast.makeText(
                    context,
                    getText(R.string.service_error),
                    Toast.LENGTH_LONG
                )
                    .show()
            }

        })
        Log.i("user", user.toString())
    }

    /**
     * Metodo que modifica al usuario si los campos son correctos
     */
    private fun updateUser() {
        if (UtilText.isMailValid(txtEmailMyProfile.text.toString())) {
            if (UtilText.isPasswordValid(txtPassMyprofile.text.toString())) {
                if (!someIsEmpty()) {
                    dialogUpdate()
                }
            } else {
                txtInLaMyprofilePass.error = resources.getString(R.string.pwd_incorrecto)
            }
        } else {
            txtInLaMyprofileEmail.error = resources.getString(R.string.email_incorrecto)
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
     * Método que devuelve true si alguno de los valores está vácio
     */
    private fun someIsEmpty(): Boolean {
        var valid = false
        if (UtilText.empty(txtEmailMyProfile, txtInLaMyprofileName, context!!)
            || UtilText.empty(txtPassMyprofile, txtInLaMyprofilePass, context!!) || UtilText.empty(
                txtUserNameMyProfile,
                txtInLaMyprofileName,
                context!!
            )
        ) {
            valid = true
        }
        return valid
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
                delUser()

            }
            .setNegativeButton(getString(R.string.Cancel), null)
            .show()
    }

    private fun delUser() {
        val turistREST = TuristAPI.service
        val call: Call<UserDTO> = turistREST.userDelete(USER.id)
        call.enqueue((object : Callback<UserDTO> {

            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                Log.i("REST", "onResponse delUser")
                if (response.isSuccessful) {
                    Log.i("REST", "isSuccessful delUser")
                    UtilSession.closeSession(context!!)
                    startActivity(Intent(context, LoginActivity::class.java))
                    activity!!.finish()
                    Toast.makeText(context!!, getText(R.string.userDelete), Toast.LENGTH_SHORT).show()
                    Log.i("REST", "sesionDelete ok")
                } else {
                    Log.i("REST", "Error: isSuccessful delUser")
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                Log.i("REST", "delUser failure")
                Toast.makeText(
                    context,
                    getText(R.string.service_error),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }))
    }

    private fun dialogUpdate() {
        AlertDialog.Builder(context)
            .setTitle(getText(R.string.caution))
            .setMessage(getText(R.string.sure_update))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                Log.i("updater", "usuario cambia")
                update()
            }
            .setNegativeButton(getString(R.string.Cancel), null)
            .show()
    }

    /**
     * Coge la informacion del usuario y lo muestra en los cuadros de texto
     */
    private fun getInformation() {
        txtEmailMyProfile.setText(USER.email)
        txtUserNameMyProfile.setText(USER.name)
        txtUserNameMyProfile.setText(USER.nameUser)
        if (USER.image != "") {
            Log.i("util", "Carga imagen")
            imgMyprofile.setImageBitmap(UtilImage.toBitmap(USER.image))
            UtilImage.redondearFoto(imgMyprofile)
        }

    }
    //************************************************************
    //METODOS PARA LA IMAGEN**************************************
    /**
     * Inicia los eventos de los botones
     */
    private fun initButtoms() {
        imgMyprofile.setOnClickListener { initDialogPhoto() }
        imgEmailMyProfile?.setOnClickListener { shareGmail() }
        imgFaceMyProfile?.setOnClickListener { shareSite("com.facebook.katana") }
        imgTwitterMyProfile?.setOnClickListener { shareSite("com.twitter.android") }
        imgInstaMyProfile?.setOnClickListener { shareSite("com.instagram.android") }
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
                    0 -> {
                        if ((activity!!.application as MyApplication).initPermissesGallery()) {
                            takephotoFromGallery()
                        } else {
                            (activity!!.application as MyApplication).initPermissesGallery()
                        }
                    }
                    1 -> {
                        if ((activity!!.application as MyApplication).initPermissesCamera()) {
                            takePhotoFromCamera()
                        } else {
                            (activity!!.application as MyApplication).initPermissesCamera()
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
                    FOTO = Bitmap.createScaledBitmap(FOTO, 100 /*Ancho*/, 100 /*Alto*/, false /* filter*/)

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
                FOTO = Bitmap.createScaledBitmap(FOTO, 100 /*Ancho*/, 100 /*Alto*/, false /* filter*/)

                // Mostramos la imagen
                imgMyprofile.setImageBitmap(FOTO)
                UtilImage.redondearFoto(imgMyprofile)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (ex: Exception) {
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


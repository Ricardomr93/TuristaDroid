package android.ricardoflor.turistdroid.activities.ui.myprofile

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
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
import android.ricardoflor.turistdroid.activities.LoginActivity
import android.ricardoflor.turistdroid.activities.NavigationActivity
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.user.UserController
import android.ricardoflor.turistdroid.bd.user.UserDTO
import android.ricardoflor.turistdroid.utils.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.loader.content.AsyncTaskLoader
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.NullPointerException
import java.time.Instant


class MyProfileFragment : Fragment() {
    // Constantes
    private val GALLERY = 1
    private val CAMERA = 2
    lateinit var IMAGE: Uri
    private val IMAGEN_DIR = "/TuristDroid"
    private lateinit var FOTO: Bitmap
    private lateinit var IMAGEN_NOMBRE: String

    //proveedor
    var provider: String = ""
    lateinit var imageProvider: ImageView

    //storage
    lateinit var storage: FirebaseStorage
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = context!!.getSharedPreferences("TuristDroid", Context.MODE_PRIVATE)
        provider = prefs.getString("provider", "").toString()
        Log.i("fairebase", "provider: $provider")
        init()
    }

    private fun init() {
        storage = Firebase.storage
        getInformation()
        if (provider != NavigationActivity.ProviderType.BASIC.name) {
            btnUpdateMyprofile.isVisible = false
            btnUnsubMyProfile.isVisible = false
            txtInLaMyprofilePass.isVisible = false
            txtEmailMyProfile.isClickable = false
            if (provider == NavigationActivity.ProviderType.GOOGLE.name) {
                lblMyProfileProvider.text = "$provider \n account"
                imgMyProfileProvider.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_google_round))
                txtUserNameMyProfile.isEnabled = false
                txtEmailMyProfile.isEnabled = false
            }
        } else {
            lblMyProfileProvider.isVisible = false
            imgMyProfileProvider.isVisible = false
            btnUpdateMyprofile.setOnClickListener {
                UtilText.cleanErrors(txtInLaMyprofileEmail, txtInLaMyprofileName, txtInLaMyprofilePass)
                buttomUpdate()
            }
            btnUnsubMyProfile.setOnClickListener {
                UtilText.cleanErrors(txtInLaMyprofileEmail, txtInLaMyprofileName, txtInLaMyprofilePass)
                buttomDelete()
            }
            initButtoms()
        }
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
     * Metodo que modifica al usuario si los campos son correctos
     */
    private fun updateUser() {
        if (UtilText.isMailValid(txtEmailMyProfile.text.toString())) {
            if (UtilText.isPasswordValid(txtPassMyprofile.text.toString()) || txtPassMyprofile.text.isNullOrEmpty() && !someIsEmpty()) {
                progressBarMyProfile.visibility
                dialogUpdate()
            }
        } else {
            txtInLaMyprofileEmail.error = resources.getString(R.string.email_incorrecto)
        }
    }

    /**
     * Método que cambia los datos del usuario en el navigation
     */
    fun changeNavigation() {
        val user = Firebase.auth.currentUser
        NavigationActivity.navUsername.text = user!!.displayName
        NavigationActivity.navUserEmail.text = user!!.email
        if (user.photoUrl != null) {
            Log.i("util", "Carga imagen")
            Picasso.get()
                .load(user.photoUrl)
                .transform(RoundImagePicasso())
                .into(NavigationActivity.navUserImage)
        }
        Log.i("fairebase","cambia navigation ${user.photoUrl}")
    }

    /**
     * Método que devuelve true si alguno de los valores está vácio
     */
    private fun someIsEmpty(): Boolean {
        var valid = false
        if (UtilText.empty(txtEmailMyProfile, txtInLaMyprofileName, context!!)
            || UtilText.empty(
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
                delUser()

            }
            .setNegativeButton(getString(R.string.Cancel), null)
            .show()
    }

    private fun delUser() {
        val user = Firebase.auth.currentUser
        // Create a storage reference from our app
        val imageRef = storage.reference.child("images/users/${user!!.uid}.jpg")
        imageRef.delete().addOnSuccessListener {}.addOnFailureListener {}
        user!!.delete()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("fairebase", "User account deleted.")
                } else {
                    startActivity(Intent(context, LoginActivity::class.java))
                    activity!!.finish()
                    Toast.makeText(context!!, getText(R.string.userDelete), Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun update() {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = txtUserNameMyProfile.text.toString()
            loadImage(displayName!!, user!!)
        }
        updateEmailAndPassword()
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    changeNavigation()
                    Toast.makeText(context, getText(R.string.update_user), Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "User profile updated.")
                } else {
                    Toast.makeText(context, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun updateEmailAndPassword() {
        val user = Firebase.auth.currentUser
        val email = txtEmailMyProfile.text.toString()
        if (user!!.email != email) {
            user.updateEmail(email).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("fairebase", "email cambiado")
                } else {
                    errorEmpass(it.exception!!.message.toString())
                    Log.i("fairebase", "email no cambiado error")
                }
            }
        }
        if (!txtPassMyprofile.text.toString().isEmpty() && UtilText.isPasswordValid(txtPassMyprofile.text.toString())
        ) {
            val pass = UtilEncryptor.encrypt(txtPassMyprofile.text.toString())!!
            user.updatePassword(pass).addOnCompleteListener {
                if (!it.isSuccessful) {
                    errorEmpass(it.exception!!.message.toString())
                    Log.i("fairebase", "email no cambiado error")
                }
            }
        }
    }

    private fun errorEmpass(string: String) {
        AlertDialog.Builder(context).setTitle(getText(R.string.caution))
            .setMessage(string).setPositiveButton(getText(R.string.ok)) { _, _ ->
            }.show()
    }

    private fun loadImage(string: String, user: FirebaseUser) {
        if (!this::FOTO.isInitialized) {
            return
        }
        val baos = ByteArrayOutputStream()
        FOTO.compress(Bitmap.CompressFormat.JPEG, 40, baos)
        val data = baos.toByteArray()
        val imageRef = storage.reference.child("images/users/${user.uid}.jpg")
        var uploadTask = imageRef.putBytes(data)
        //descarga y referencia URl
        uploadTask.addOnFailureListener {
            Log.i("fairebase", "error al subir la foto a storage")
        }.addOnSuccessListener { taskSnapshot ->
            val dowuri = taskSnapshot.metadata!!.reference!!.downloadUrl
            dowuri.addOnSuccessListener { task ->
                val profileUpdates = userProfileChangeRequest {
                    photoUri = task
                    Log.i("fairebase", "uri: $task")
                }
                //modifica con los cambios de la uri
                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            changeNavigation()
                            Log.d("TAG", "uri profile good")
                        }
                    }
            }
        }

    }

    /**
     * Coge la informacion del usuario y lo muestra en los cuadros de texto
     */
    private fun getInformation() {
        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl
            Log.i("fairebase", "photoUrl: $photoUrl")
            if (photoUrl != null) {
                Picasso.get()
                    .load(photoUrl)
                    .transform(RoundImagePicasso())
                    .into(imgMyprofile)
            }
            //cambiamos los valores por los del usuario
            txtEmailMyProfile.setText(email)
            txtUserNameMyProfile.setText(name)
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
                    FOTO = UtilImage.scaleImage(FOTO, 800, 800)
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
                FOTO = UtilImage.scaleImage(FOTO, 800, 800)
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


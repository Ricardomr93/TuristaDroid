package android.ricardoflor.turistdroid.activities


import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.utils.UtilEncryptor
import android.ricardoflor.turistdroid.utils.UtilNet
import android.ricardoflor.turistdroid.utils.UtilText
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    //autenticador
    private lateinit var auth: FirebaseAuth

    //google
    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SING_IN = 100
    var email: String = ""
    var pass: String = ""

    //tipo proveedor
    enum class ProviderType {
        BASIC,
        GOOGLE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        initGoogle()
        buttonLoginLogin.setOnClickListener {
            UtilText.cleanErrors(txtInLaLoginPass, txtInLaLoginEmail)
            login()
        }
        buttomGoogleLogin.setOnClickListener {
            loginGoogle()
        }
        SingIn()
    }

    private fun initGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
    }
    /*
    * ****************************************************
    * GOOGLE AUTH
    * ****************************************************
    * */
    private fun loginGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SING_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SING_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("fairebase", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("fairebase", "Google sign in failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("fairebase", "signInWithCredential:success")
                    val user = auth.currentUser
                    toNavigation(ProviderType.GOOGLE)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("fairebase", "signInWithCredential:failure", task.exception)
                }
            }
    }

    /*
    * ****************************************************
    * BASIC AUTH
    * ****************************************************
    * */

    /**
     * Método que cuando pulsa en en el boton si lo campos son correctos
     * logea al usuario
     */
    private fun login() {
        email = editTextLoginMail.text.toString()
        pass = UtilEncryptor.encrypt(editTextLoginPassword.text.toString())!!

        if (anyEmpty()) {
            if (UtilNet.hasInternetConnection(this)) {
                userExists(email, pass)
            } else {//muestra una barra para pedir conexion a internet
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
            Log.i("realm", "usuario logeado")
        }
    }

    /**
     * Método que devuelve false si alguno de los valores está vácio
     */
    private fun anyEmpty(): Boolean {
        var valid = true
        if (UtilText.empty(editTextLoginMail, txtInLaLoginEmail, this) || UtilText.empty(
                editTextLoginPassword,
                txtInLaLoginPass,
                this
            )
        ) {
            valid = false
            Log.i("valido", "alguno vacio")
        }
        return valid
    }

    /**
     * Funcion onClick del botón Singin para llevarlo a la actividad
     */
    private fun SingIn() {
        buttonLoginSingin.setOnClickListener {
            val intent = Intent(this, SinginActivity::class.java).apply {
            }
            startActivity(intent)
        }
    }

    /**
     * Método que busca por email y si lo encuentra
     * lo compara con la contraseña
     */
    private fun userExists(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.i("fairbase", "signInWithEmail:success")
                    val user = auth.currentUser
                    Log.i("fairbase", user.toString())
                    //Toast.makeText(baseContext, "Auth: Usuario autentificado con éxito", Toast.LENGTH_SHORT).show()
                    toNavigation(ProviderType.BASIC)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("fairbase", "signInWithEmail:failure", task.exception)
                    txtInLaLoginPass.error = resources.getString(R.string.userNotCorrect)
                }

            }
    }

    /**
     * Metodo que para ir al navigation
     */
    private fun toNavigation(provider: ProviderType) {
        val intent = Intent(applicationContext, NavigationActivity::class.java).apply {
            putExtra("provider", provider.name)
        }
        startActivity(intent)
        finish()
    }

    /**
     * Método sobreescrito que salva el estado en el ciclo del vida
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // Salvamos en un bundle estas variables o estados de la interfaz
        outState.run {
            // Actualizamos los datos o los recogemos de la interfaz
            putString("EMAIL", email)
            putString("PASSWORD", pass)
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
            email = getString("EMAIL").toString()
            pass = getString("PASSWORD").toString()
        }
    }
}
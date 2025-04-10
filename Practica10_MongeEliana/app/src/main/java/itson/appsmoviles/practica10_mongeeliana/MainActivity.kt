package itson.appsmoviles.practica10_mongeeliana

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    object Global {
        var preferencias_compartidas = "sharedpreferences"
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnCrearCuenta = findViewById<Button>(R.id.btn_crearCuenta)
        val btnLoginGoogle = findViewById<Button>(R.id.btnLoginGoogle)

        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword)


        verificar_sesion_abierta()

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString()
            val pass = etPassword.text.toString()

            if (correo.isNotEmpty() && pass.isNotEmpty()) {
                login_firebase(correo, pass)
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            }
        }
        btnLoginGoogle.setOnClickListener {
            iniciarSesionConGoogle()
        }


        btnCrearCuenta.setOnClickListener {
            val correo = etCorreo.text.toString()
            val pass = etPassword.text.toString()

            if (correo.isNotEmpty() && pass.isNotEmpty()) {
                crearCuentaFirebase(correo, pass)
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun verificar_sesion_abierta() {
        val sesion_abierta: SharedPreferences = getSharedPreferences(
            Global.preferencias_compartidas,
            Context.MODE_PRIVATE
        )
        val correo = sesion_abierta.getString("Correo", null)
        val proveedor = sesion_abierta.getString("Proveedor", null)
        if (correo != null && proveedor != null) {
            val intent = Intent(applicationContext, Bienvenida::class.java)
            intent.putExtra("Correo", correo)
            intent.putExtra("Proveedor", proveedor)
            startActivity(intent)
        }
    }

    private fun guardar_sesion(correo: String, proveedor: String) {
        val guardar_sesion: SharedPreferences.Editor = getSharedPreferences(
            Global.preferencias_compartidas,
            Context.MODE_PRIVATE
        ).edit()
        guardar_sesion.putString("Correo", correo)
        guardar_sesion.putString("Proveedor", proveedor)
        guardar_sesion.apply()
    }

    fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val credencial =
                            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                        FirebaseAuth.getInstance().signInWithCredential(credencial)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(applicationContext, Bienvenida::class.java)
                                    intent.putExtra("Correo", task.result.user?.email)
                                    intent.putExtra("Proveedor", "Google")
                                    startActivity(intent)
                                    guardar_sesion(task.result.user?.email.toString(), "Google")
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error en la autenticación con Firebase",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } catch (e: GoogleIdTokenParsingException) {
                        Toast.makeText(
                            applicationContext,
                            "Error al parsear el token de Google: $e",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            else -> {
                Toast.makeText(
                    applicationContext,
                    "Error: Credencial no reconocida",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun iniciarSesionConGoogle() {
        val credentialManager = CredentialManager.create(this)

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(getString(R.string.web_client))
            .setNonce("nonce")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@MainActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al obtener la credencial: $e",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }



    fun login_firebase(correo: String, pass: String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(applicationContext, Bienvenida::class.java)
                    intent.putExtra("Correo", task.result.user?.email)
                    intent.putExtra("Proveedor", "Email")
                    startActivity(intent)
                    guardar_sesion(task.result.user?.email.toString(), "Usuario/Contraseña")
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Error en la autenticación con Firebase",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun crearCuentaFirebase(correo: String, pass: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(applicationContext, Bienvenida::class.java)
                    intent.putExtra("Correo", task.result.user?.email)
                    intent.putExtra("Proveedor", "Email")
                    startActivity(intent)
                    guardar_sesion(task.result.user?.email.toString(), "Usuario/Contraseña")
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Error al crear la cuenta: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

}

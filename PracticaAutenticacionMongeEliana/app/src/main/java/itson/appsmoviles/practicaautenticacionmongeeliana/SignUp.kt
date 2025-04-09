package itson.appsmoviles.practicaautenticacionmongeeliana

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = Firebase.auth

        val email: EditText = findViewById(R.id.etrEmail)
        val password: EditText = findViewById(R.id.etrPassword)
        val confirmPassword: EditText = findViewById(R.id.etrConfirmPassword)
        val errorTV: TextView = findViewById(R.id.tvrError)
        val button: Button = findViewById(R.id.btnRegister)

        errorTV.visibility = View.INVISIBLE

        button.setOnClickListener {
            if (email.text.isEmpty() || password.text.isEmpty() || confirmPassword.text.isEmpty()) {
                errorTV.text = "Todos los campos deben ser llenados"
                errorTV.visibility = View.VISIBLE
            } else if (password.text.toString() != confirmPassword.text.toString()) {
                errorTV.text = "Las contraseñas no coinciden"
                errorTV.visibility = View.VISIBLE
            } else {
                errorTV.visibility = View.INVISIBLE
                signIn(email.text.toString(), password.text.toString())
            }
        }
    }

    private fun signIn(email: String, password: String) {
        Log.d("INFO", "email: $email, password: $password")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("INFO", "signInWithEmail:success")

                    // Enviar email a MainActivity
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("user", email)
                    }

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish() // Cierra SignUp para que no pueda regresar
                } else {
                    Log.w("ERROR", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "El registro falló: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}

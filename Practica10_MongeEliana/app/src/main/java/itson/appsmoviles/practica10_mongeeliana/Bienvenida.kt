package itson.appsmoviles.practica10_mongeeliana

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Bienvenida : AppCompatActivity() {
    val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)



        val tvCorreo = findViewById<TextView>(R.id.evCorreo)
        val tvProveedor = findViewById<TextView>(R.id.evProovedor)
        val btnSalir = findViewById<Button>(R.id.btnSalir)

        tvCorreo.text = "Correo: ${user?.email}"
        val proveedorReal = user?.providerData
            ?.firstOrNull { it.providerId != "firebase" }
            ?.providerId ?: "Desconocido"

        tvProveedor.text = "Proveedor: $proveedorReal"




        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {

            tvCorreo.text = "Correo: ${user.email}"
            tvProveedor.text = "Proveedor: $proveedorReal"
        } else {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        btnSalir.setOnClickListener {
            // 1. Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Borrar la sesión guardada en SharedPreferences
            val preferencias = getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
            val editor = preferencias.edit()
            editor.clear() // Esto borra todo lo guardado
            editor.apply()

            // 3. Mostrar mensaje
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            // 4. Redirigir a MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
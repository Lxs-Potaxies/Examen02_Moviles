package com.example.a14_firebaseaccess

import CategoryAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a14_firebaseaccess.entities.cls_Category
import com.example.a14_firebaseaccess.ui.users.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

const val valorIntentLogin = 1
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var email: String? = null
    private var contra: String? = null
    private var TAG = "YorkTestingApp"
    private lateinit var btnLogOut: Button
    private lateinit var btnMenuDeCompras: Button

    // Reemplaza startActivityForResult con registerForActivityResult
    private val loginActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            obtenerDatos() // Cargar datos al regresar del Login con éxito
        } else {
            LogOut() // Cerrar sesión si el resultado no es OK
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnLogOut = findViewById(R.id.btnLogOut)
        btnMenuDeCompras = findViewById(R.id.btnMenuDeCompras)

        val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)
        email = prefe.getString("email", "")
        contra = prefe.getString("contra", "")

        if (email.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            loginActivityLauncher.launch(intent)
        } else {
            if (auth.currentUser == null) {
                auth.signInWithEmailAndPassword(email.toString(), contra.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Autenticación correcta", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.w(TAG, "Error de autenticación: ", it.exception)
                        }
                    }
            }
            obtenerDatos()
        }

        btnLogOut.setOnClickListener {
            LogOut()
        }

        btnMenuDeCompras.setOnClickListener {
            MenuDeCompras()
        }
    }

    private fun LogOut() {
        auth.signOut()
        Toast.makeText(this, "Cerrar sesión exitosa", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        loginActivityLauncher.launch(intent)
        finish()
    }

    private fun MenuDeCompras() {
        val intent = Intent(this, PantallaCompra::class.java)
        startActivity(intent)
    }

    private fun obtenerDatos() {
        val coleccion: ArrayList<cls_Category> = ArrayList()
        val listaView: ListView = findViewById(R.id.lstCategories)

        db.collection("Categories").orderBy("CategoryID")
            .get()
            .addOnCompleteListener { docc ->
                if (docc.isSuccessful) {
                    docc.result?.let { result ->
                        for (document in result) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            val datos = cls_Category(
                                document.data["CategoryID"].toString().toIntOrNull() ?: 0,
                                document.data["CategoryName"].toString(),
                                document.data["Description"].toString(),
                                document.data["urlImage"].toString()
                            )
                            coleccion.add(datos)
                        }
                        val adapter = CategoryAdapter(this, coleccion)
                        listaView.adapter = adapter
                    } ?: Log.w(TAG, "No se encontraron documentos en la colección Categories.")
                } else {
                    Log.w(TAG, "Error al obtener los documentos.", docc.exception)
                    Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

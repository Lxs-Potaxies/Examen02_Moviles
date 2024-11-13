package com.example.a14_firebaseaccess.ui.users

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a14_firebaseaccess.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date


const val valorIntentSignup = 1

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()
    private lateinit var btnAutenticar: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var txtRegister: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var txtShipVia: EditText
    private lateinit var txtShipName: EditText
    private lateinit var txtShipAddress: EditText
    private lateinit var txtShipCity: EditText
    private lateinit var txtShipRegion: EditText
    private lateinit var txtShipPostalCode: EditText
    private lateinit var txtShipCountry: EditText
    private lateinit var btnRecuperar: Button
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnAutenticar = findViewById(R.id.btnAutenticar)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtContra)
        txtRegister = findViewById(R.id.txtRegister)

        txtRegister.setOnClickListener {
            goToSignup()
        }

        btnAutenticar.setOnClickListener {
            if (txtEmail.text.isNotEmpty() && txtContra.text.isNotEmpty()) {
                auth.signInWithEmailAndPassword(txtEmail.text.toString(), txtContra.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val dt = Date()

                            val user = hashMapOf(
                                "ultAcceso" to dt.toString()
                            )

                            db.collection("datosUsuarios")
                                .whereEqualTo("idemp", it.result?.user?.uid.toString()).get()
                                .addOnSuccessListener { documentReference ->
                                    documentReference.forEach { document ->
                                        db.collection("datosUsuarios").document(document.id)
                                            .update(user as Map<String, Any>)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error al actualizar los datos del usuario",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            val prefe = this.getSharedPreferences("appData", Context.MODE_PRIVATE)

                            val editor = prefe.edit()

                            editor.putString("email", txtEmail.text.toString())
                            editor.putString("contra", txtContra.text.toString())
                            editor.apply()
                            Intent().let {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            "Error".showAlert("Al autenticar el usuario")
                        }
                    }
            } else {
                "Error".showAlert("El correo electrónico y contraseña no pueden estar vacíos")
            }
        }

        sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE)

        txtShipVia = findViewById(R.id.txtShipVia)
        txtShipName = findViewById(R.id.txtShipName)
        txtShipAddress = findViewById(R.id.txtShipAddress)
        txtShipCity = findViewById(R.id.txtShipCity)
        txtShipRegion = findViewById(R.id.txtShipRegion)
        txtShipPostalCode = findViewById(R.id.txtShipPostalCode)
        txtShipCountry = findViewById(R.id.txtShipCountry)
        btnRecuperar = findViewById(R.id.btnRecuperar)

        btnRecuperar.setOnClickListener {
            recuperarDatosDesdeLocalStorage()
        }
    }


    private fun goToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivityForResult(intent, valorIntentSignup)
    }

    private fun String.showAlert(mssg: String = "Error") {
        val diagMessage = AlertDialog.Builder(this@LoginActivity)
        diagMessage.setTitle(this)
        diagMessage.setMessage(mssg)
        diagMessage.setPositiveButton("Aceptar", null)
        val diagVentana: AlertDialog = diagMessage.create()
        diagVentana.show()
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            Intent().let {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun recuperarDatosDesdeLocalStorage() {
        val shipVia = sharedPreferences.getString("shipVia", "")
        val shipName = sharedPreferences.getString("shipName", "")
        val shipAddress = sharedPreferences.getString("shipAddress", "")
        val shipCity = sharedPreferences.getString("shipCity", "")
        val shipRegion = sharedPreferences.getString("shipRegion", "")
        val shipPostalCode = sharedPreferences.getString("shipPostalCode", "")
        val shipCountry = sharedPreferences.getString("shipCountry", "")

        txtShipVia.setText(shipVia)
        txtShipName.setText(shipName)
        txtShipAddress.setText(shipAddress)
        txtShipCity.setText(shipCity)
        txtShipRegion.setText(shipRegion)
        txtShipPostalCode.setText(shipPostalCode)
        txtShipCountry.setText(shipCountry)
    }
}
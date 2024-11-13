package com.example.a14_firebaseaccess.ui.users

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.a14_firebaseaccess.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date


@Suppress("NAME_SHADOWING")
class SignupActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()

    private lateinit var txtRNombre: EditText
    private lateinit var txtREmail: EditText
    private lateinit var txtRContra: EditText
    private lateinit var txtRreContra: EditText
    private lateinit var btnRegistrarU: Button
    private lateinit var txtCustomerID: EditText
    private lateinit var txtCompanyName: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        txtRNombre = findViewById(R.id.txtRNombre)
        txtREmail = findViewById(R.id.txtREmail)
        txtRContra = findViewById(R.id.txtRContra)
        txtRreContra = findViewById(R.id.txtRreContra)
        btnRegistrarU = findViewById(R.id.btnRegistrarU)
        txtCustomerID = findViewById(R.id.txtCustomerID)
        txtCompanyName = findViewById(R.id.txtCompanyName)



        btnRegistrarU.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = txtRNombre.text.toString()
        val email = txtREmail.text.toString()
        val contra = txtRContra.text.toString()
        val reContra = txtRreContra.text.toString()
        val customerID = txtCustomerID.text.toString().trim().uppercase()
        val companyName = txtCompanyName.text.toString()

        if (nombre.isEmpty() || email.isEmpty() || contra.isEmpty() || reContra.isEmpty() || customerID.isEmpty() || companyName.isEmpty()) {
            Toast.makeText(this, "Favor de llenar todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (contra != reContra) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val customersCollection = db.collection("Customers")

        customersCollection.whereEqualTo("CustomerID", customerID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                        // Si existe el CustomerID, entonces continúa con el proceso
                        val documentId = querySnapshot.documents[0].id
                        val updateData = mapOf(
                            "ContactName" to nombre,
                            "CompanyName" to companyName
                        )
                        customersCollection.document(documentId)
                            .update(updateData)
                            .addOnSuccessListener {
                                val ordersCollection = db.collection("Orders")
                                ordersCollection
                                    .whereEqualTo("CustomerID", customerID)
                                    .get()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val querySnapshot = task.result
                                            if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                                                val document = querySnapshot.documents[0]
                                                Log.d("TuTag", document.toString())
                                                val shipVia = document.get("ShipVia")
                                                val shipName = document.get("ShipName")
                                                val shipAddress = document.get("ShipAddress")
                                                val shipCity = document.get("ShipCity")
                                                val shipRegion = document.get("ShipRegion")
                                                val shipPostalCode = document.get("ShipPostalCode")
                                                val shipCountry = document.get("ShipCountry")

                                                // Crear cuenta de usuario en Firebase Auth
                                                auth.createUserWithEmailAndPassword(email, contra)
                                                    .addOnCompleteListener(this) { authTask ->
                                                        if (authTask.isSuccessful) {
                                                            val dt = Date()
                                                            val user = hashMapOf(
                                                                "idemp" to authTask.result?.user?.uid,
                                                                "usuario" to nombre,
                                                                "email" to email,
                                                                "CustomerID" to customerID,
                                                                "ultAcceso" to dt.toString(),
                                                            )

                                                            db.collection("datosUsuarios")
                                                                .add(user)
                                                                .addOnSuccessListener {
                                                                    val prefe = this.getSharedPreferences(
                                                                        "appData",
                                                                        Context.MODE_PRIVATE
                                                                    )
                                                                    prefe.edit {
                                                                        putString("email", email)
                                                                        putString("contra", contra)
                                                                        putString("shipVia", shipVia?.toString() ?: "")
                                                                        putString("shipName", shipName?.toString() ?: "")
                                                                        putString("shipAddress", shipAddress?.toString() ?: "")
                                                                        putString("shipCity", shipCity?.toString() ?: "")
                                                                        putString("shipRegion", shipRegion?.toString() ?: "")
                                                                        putString("shipPostalCode", shipPostalCode?.toString() ?: "")
                                                                        putString("shipCountry", shipCountry?.toString() ?: "")
                                                                    }
                                                                    Toast.makeText(
                                                                        this,
                                                                        "Usuario registrado correctamente",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()

                                                                    setResult(Activity.RESULT_OK)
                                                                    finish()
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    handleError("Error al registrar usuario: ${e.message}")
                                                                }
                                                        } else {
                                                            handleError("Error al registrar usuario: ${authTask.exception}")
                                                        }
                                                    }
                                            }
                                        }
                                    }
                            }
                            .addOnFailureListener { e ->
                                handleError("Error al modificar el documento: ${e.message}")
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "El Customer con ID $customerID no existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    handleError("Error al realizar la consulta: ${task.exception?.message}")
                }
            }
    }

    private fun handleError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

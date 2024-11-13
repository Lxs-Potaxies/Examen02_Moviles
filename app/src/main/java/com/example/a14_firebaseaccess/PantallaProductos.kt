package com.example.a14_firebaseaccess

import ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a14_firebaseaccess.entities.cls_Product
import com.google.firebase.firestore.FirebaseFirestore

class PantallaProductos : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private lateinit var listaView: ListView
    private lateinit var adapter: ProductAdapter
    private lateinit var btnConfirmarSeleccion: Button
    private val coleccion = mutableListOf<cls_Product>()
    private val productosSeleccionados = mutableListOf<cls_Product>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_productos)

        listaView = findViewById(R.id.lstProducts)
        btnConfirmarSeleccion = findViewById(R.id.btnConfirmarSeleccion)

        obtenerDatos()

        btnConfirmarSeleccion.setOnClickListener {
            Log.d("PantallaProductos", "Botón Confirmar Selección presionado")
            if (productosSeleccionados.isNotEmpty()) {
                val resultIntent = Intent().apply {
                    putParcelableArrayListExtra("productosSeleccionados", ArrayList(productosSeleccionados))
                }
                setResult(RESULT_OK, resultIntent)
                finish()
                Log.d("PantallaProductos", "setResult ejecutado y actividad finalizada con finish()")
            } else {
                Toast.makeText(this, "No hay productos seleccionados", Toast.LENGTH_SHORT).show()
                Log.d("PantallaProductos", "Intento de confirmar sin productos seleccionados")
            }
        }
    }

    private fun obtenerDatos() {
        db.collection("Products").get()
            .addOnSuccessListener { documents ->
                coleccion.clear()
                for (document in documents) {
                    val productId = document.get("ProductID")?.toString() ?: "N/A"
                    val productName = document.getString("ProductName") ?: "N/A"
                    val unitPrice = document.get("UnitPrice")?.toString()?.toDoubleOrNull() ?: 0.0
                    val quantityPerUnit = document.get("QuantityPerUnit")?.toString()?.toIntOrNull() ?: 1

                    val product = cls_Product(
                        ProductID = productId,
                        ProductName = productName,
                        UnitPrice = unitPrice,
                        QuantityPerUnit = quantityPerUnit.toDouble()
                    )
                    coleccion.add(product)
                }
                Log.d("PantallaProductos", "Productos obtenidos: ${coleccion.size}")
                adapter = ProductAdapter(this@PantallaProductos, coleccion, productosSeleccionados)
                listaView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("PantallaProductos", "Error obteniendo productos: ", exception)
                Toast.makeText(this, "Error al obtener datos de productos.", Toast.LENGTH_SHORT).show()
            }
    }
}

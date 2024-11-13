package com.example.a14_firebaseaccess

import OnProductUpdatedListener
import SelectedProductAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a14_firebaseaccess.entities.cls_Product
import com.example.a14_firebaseaccess.database.SQLiteHelper
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.Timestamp

class PantallaCompra : AppCompatActivity(), OnProductUpdatedListener {

    private lateinit var btnAgregarProducto: Button
    private lateinit var listProductosSeleccionados: ListView
    private lateinit var txtSubTotal: TextView
    private val productosSeleccionados = mutableListOf<cls_Product>()
    private lateinit var productLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore
    private lateinit var sqliteHelper: SQLiteHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_compra)

        btnAgregarProducto = findViewById(R.id.btnAgregarProducto)
        listProductosSeleccionados = findViewById(R.id.listProductosSeleccionados)
        txtSubTotal = findViewById(R.id.txtSubTotal)
        sqliteHelper = SQLiteHelper(this)
        sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE)

        listProductosSeleccionados.adapter = SelectedProductAdapter(this, productosSeleccionados, this)

        btnAgregarProducto.setOnClickListener {
            val intent = Intent(this, PantallaProductos::class.java)
            productLauncher.launch(intent)
        }

        productLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val productos = data?.getParcelableArrayListExtra<cls_Product>("productosSeleccionados")

                if (productos != null) {
                    productosSeleccionados.addAll(productos)
                    updateSelectedProductsList()
                    saveProductsToSQLite(productos) // Guardar productos en SQLite
                } else {
                    Toast.makeText(this, "No se recibieron productos seleccionados", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.btnAplicarCompra).setOnClickListener {
            applyPurchase()
        }

        findViewById<Button>(R.id.btnCancelarCompra).setOnClickListener {
            cancelPurchase()
        }
    }

    override fun onProductUpdated() {
        actualizarSubtotal()
    }

    private fun updateSelectedProductsList() {
        listProductosSeleccionados.adapter = SelectedProductAdapter(this, productosSeleccionados, this)
        actualizarSubtotal()
    }

    private fun actualizarSubtotal() {
        var subtotal = 0.0
        for (producto in productosSeleccionados) {
            val precio = producto.UnitPrice ?: 0.0
            val cantidad = producto.QuantityPerUnit
            subtotal += precio * cantidad
        }
        txtSubTotal.text = "Subtotal: %.2f".format(subtotal)
    }

    private fun saveProductsToSQLite(products: List<cls_Product>) {
        for (producto in products) {
            sqliteHelper.addProduct(producto)
        }
    }

    private fun applyPurchase() {
        val db = FirebaseFirestore.getInstance()
        val sharedPrefs = getSharedPreferences("appData", Context.MODE_PRIVATE)
        var customerID = sharedPrefs.getString("CustomerID", null)
        val email = sharedPrefs.getString("email", null)
        if (customerID.isNullOrEmpty()) {
            if (email.isNullOrEmpty()) {
                Toast.makeText(this, "Correo no disponible en SharedPreferences", Toast.LENGTH_SHORT).show()
                return
            }
            db.collection("datosUsuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        customerID = document.getString("CustomerID")

                        if (!customerID.isNullOrEmpty()) {
                            sharedPrefs.edit().putString("CustomerID", customerID).apply()
                            createOrder(customerID!!)
                        } else {
                            Toast.makeText(this, "CustomerID no encontrado en el documento", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al buscar CustomerID: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            createOrder(customerID!!)
        }
    }

    private fun createOrder(customerID: String) {
        val db = FirebaseFirestore.getInstance()

        // Setear valores desde SharedPreferences
        val employeeID = 1
        val freight = 27.15

        // Cambiar `getInt` a `getString` y convertir a Int
        val shipVia = sharedPreferences.getString("shipVia", "0")?.toIntOrNull() ?: 0
        val shipName = sharedPreferences.getString("shipName", "")
        val shipAddress = sharedPreferences.getString("shipAddress", "")
        val shipCity = sharedPreferences.getString("shipCity", "")
        val shipRegion = sharedPreferences.getString("shipRegion", "")
        val shipPostalCode = sharedPreferences.getString("shipPostalCode", "0")?.toIntOrNull() ?: 0
        val shipCountry = sharedPreferences.getString("shipCountry", "")

        if (!areOrderDetailsValid(customerID, employeeID, freight, shipVia,
                shipName, shipAddress, shipCity, shipRegion, shipPostalCode, shipCountry)) {
            Toast.makeText(this, "Error: Campos incompletos en la orden", Toast.LENGTH_SHORT).show()
            return
        }

        val orderDate = Timestamp.now()
        val requiredDate = Timestamp.now()
        val shippedDate = Timestamp.now()
        val newOrder = hashMapOf(
            "CustomerID" to customerID,
            "EmployeeID" to employeeID,
            "Freight" to freight,
            "OrderDate" to orderDate,
            "RequiredDate" to requiredDate,
            "ShippedDate" to shippedDate,
            "ShipVia" to shipVia,
            "ShipName" to shipName,
            "ShipAddress" to shipAddress,
            "ShipCity" to shipCity,
            "ShipRegion" to shipRegion,
            "ShipPostalCode" to shipPostalCode,
            "ShipCountry" to shipCountry
        )

        db.collection("Orders").add(newOrder)
            .addOnSuccessListener { orderDocRef ->
                val orderID = orderDocRef.id
                saveOrderDetails(orderID)
                Toast.makeText(this, "Orden creada exitosamente", Toast.LENGTH_SHORT).show()
                clearSelectedProducts() // Limpia la lista de productos seleccionados
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear la orden: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearSelectedProducts() {
        productosSeleccionados.clear()
        updateSelectedProductsList()
        sqliteHelper.deleteAllProducts()
        txtSubTotal.text = "Subtotal: 0.00"
    }

    private fun cancelPurchase() {
        productosSeleccionados.clear()
        updateSelectedProductsList()
        actualizarSubtotal()
        sqliteHelper.deleteAllProducts()
        Toast.makeText(this, "Compra cancelada", Toast.LENGTH_SHORT).show()
    }

    private fun saveOrderDetails(orderID: String) {
        val db = FirebaseFirestore.getInstance()
        for (producto in productosSeleccionados) {
            val newOrderDetail = hashMapOf(
                "OrderID" to orderID,
                "ProductID" to producto.ProductID,
                "Quantity" to producto.QuantityPerUnit,
                "Discount" to producto.Discount,
                "UnitPrice" to producto.UnitPrice
            )

            db.collection("OrderDetails").add(newOrderDetail)
                .addOnSuccessListener {
                    Toast.makeText(this, "Detalle de producto guardado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el detalle: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun areOrderDetailsValid(
        customerID: String,
        employeeID: Int,
        freight: Double,
        shipVia: Int,
        shipName: String?,
        shipAddress: String?,
        shipCity: String?,
        shipRegion: String?,
        shipPostalCode: Int,
        shipCountry: String?
    ): Boolean {
        var isValid = true
        if (customerID.isEmpty()) {
            Log.e("ERROR", "CustomerID está vacío")
            isValid = false
        }
        if (employeeID == 0) {
            Log.e("ERROR", "EmployeeID tiene un valor predeterminado de 0")
            isValid = false
        }
        if (freight == 0.0) {
            Log.e("ERROR", "Freight tiene un valor predeterminado de 0.0")
            isValid = false
        }
        if (shipVia == 0) {
            Log.e("ERROR", "ShipVia tiene un valor predeterminado de 0")
            isValid = false
        }
        if (shipName.isNullOrEmpty()) {
            Log.e("ERROR", "ShipName está vacío o es nulo")
            isValid = false
        }
        if (shipAddress.isNullOrEmpty()) {
            Log.e("ERROR", "ShipAddress está vacío o es nulo")
            isValid = false
        }
        if (shipCity.isNullOrEmpty()) {
            Log.e("ERROR", "ShipCity está vacío o es nulo")
            isValid = false
        }
        if (shipRegion.isNullOrEmpty()) {
            Log.e("ERROR", "ShipRegion está vacío o es nulo")
            isValid = false
        }
        if (shipPostalCode == 0) {
            Log.e("ERROR", "ShipPostalCode tiene un valor predeterminado de 0")
            isValid = false
        }
        if (shipCountry.isNullOrEmpty()) {
            Log.e("ERROR", "ShipCountry está vacío o es nulo")
            isValid = false
        }
        return isValid
    }
}

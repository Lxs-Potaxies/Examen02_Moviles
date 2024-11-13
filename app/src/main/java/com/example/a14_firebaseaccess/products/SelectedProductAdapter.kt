import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import com.example.a14_firebaseaccess.R
import com.example.a14_firebaseaccess.entities.cls_Product

interface OnProductUpdatedListener {
    fun onProductUpdated()
}

class SelectedProductAdapter(
    context: Context,
    private val productosSeleccionados: MutableList<cls_Product>,
    private val listener: OnProductUpdatedListener
) : ArrayAdapter<cls_Product>(context, 0, productosSeleccionados) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_selected_product, parent, false)

        val currentProduct = getItem(position)

        if (currentProduct != null) {
            val tvCodigo = listItemView.findViewById<TextView>(R.id.tvCodigo)
            val tvProducto = listItemView.findViewById<TextView>(R.id.tvProducto)
            val tvPrecioUnitario = listItemView.findViewById<TextView>(R.id.tvPrecioUnitario)
            val tvCantidad = listItemView.findViewById<TextView>(R.id.tvCantidad)
            val tvDescuento = listItemView.findViewById<TextView>(R.id.tvDescuento)
            val tvSubTotal = listItemView.findViewById<TextView>(R.id.tvSubTotal)

            tvCodigo.text = "Código: ${currentProduct.ProductID ?: "N/A"}"
            tvProducto.text = "Producto: ${currentProduct.ProductName ?: "N/A"}"
            tvPrecioUnitario.text = "Precio Unitario: ${currentProduct.UnitPrice ?: "0.0"}"
            tvCantidad.text = "Cantidad: ${currentProduct.QuantityPerUnit}"

            tvDescuento.text = "Descuento: ${currentProduct.Discount ?: 0.0}"

            val subtotal = calculateSubTotal(currentProduct)
            tvSubTotal.text = "Sub Total: $subtotal"

            val editCantidad = listItemView.findViewById<EditText>(R.id.editCantidad)
            val editDescuento = listItemView.findViewById<EditText>(R.id.editDescuento)

            editCantidad.setText(currentProduct.QuantityPerUnit.toString())
            editDescuento.setText(currentProduct.Discount.toString())

            editCantidad.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val cantidad = s.toString().toDoubleOrNull() ?: 1.0
                    currentProduct.QuantityPerUnit = cantidad
                    listener.onProductUpdated()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            editDescuento.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val descuento = s.toString().toDoubleOrNull() ?: 0.0
                    currentProduct.Discount = descuento
                    listener.onProductUpdated()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        return listItemView
    }

    private fun calculateSubTotal(product: cls_Product): String {
        val unitPrice = product.UnitPrice ?: 0.0
        val quantity = product.QuantityPerUnit
        val discount = product.Discount ?: 0.0
        val subtotal = unitPrice * quantity * (1 - discount / 100)
        return String.format("%.2f", subtotal)
    }
}

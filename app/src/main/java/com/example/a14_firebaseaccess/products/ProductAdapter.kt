import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.a14_firebaseaccess.R
import com.example.a14_firebaseaccess.entities.cls_Product

class ProductAdapter(
    context: Context,
    private val dataModalArrayList: List<cls_Product>,
    private val productosSeleccionados: MutableList<cls_Product>
) : ArrayAdapter<cls_Product>(context, 0, dataModalArrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)

        val currentProduct = getItem(position)

        if (currentProduct != null) {
            val tvCodigo = listItemView.findViewById<TextView>(R.id.tvCodigo)
            val tvProducto = listItemView.findViewById<TextView>(R.id.tvProducto)
            val tvPrecioUnitario = listItemView.findViewById<TextView>(R.id.tvPrecioUnitario)
            val tvCantidad = listItemView.findViewById<TextView>(R.id.tvCantidad)

            tvCodigo.text = "Código: ${currentProduct.ProductID ?: "N/A"}"
            tvProducto.text = "Producto: ${currentProduct.ProductName ?: "N/A"}"
            tvPrecioUnitario.text = "Precio Unitario: ${currentProduct.UnitPrice ?: "0.0"}"
            tvCantidad.text = "Cantidad: ${currentProduct.QuantityPerUnit}"

            val btnAgregar = listItemView.findViewById<Button>(R.id.btnAgregar)
            btnAgregar.setOnClickListener {
                if (!productosSeleccionados.contains(currentProduct)) {
                    currentProduct.QuantityPerUnit = 1.0
                    productosSeleccionados.add(currentProduct)
                    Toast.makeText(context, "${currentProduct.ProductName} agregado a la selección", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return listItemView
    }
}

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.a14_firebaseaccess.R
import com.google.firebase.storage.FirebaseStorage
import com.example.a14_firebaseaccess.entities.cls_Category

class CategoryAdapter(context: Context, dataModalArrayList: ArrayList<cls_Category>) :
    ArrayAdapter<cls_Category?>(context, 0, dataModalArrayList!! as List<cls_Category?>) {

    var imgs = FirebaseStorage.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listitemView = convertView
        if (listitemView == null) {
            listitemView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
        }

        val dataModal: cls_Category? = getItem(position)

        val categoryID = listitemView!!.findViewById<TextView>(R.id.IdCategory)
        val categoryName = listitemView!!.findViewById<TextView>(R.id.NameCategory)
        val description = listitemView.findViewById<TextView>(R.id.DescriptionCategory)

        val imageCategory = listitemView.findViewById<ImageView>(R.id.imgCategory)

        if (dataModal != null) {
            categoryID.setText(dataModal.CategoryID.toString())
            categoryName.setText(dataModal.CategoryName)
            description.setText(dataModal.Description)
            Glide.with(context).load(dataModal.urlImage).into(imageCategory)
        }

        listitemView.setOnClickListener {
            if (dataModal != null) {
                Toast.makeText(context, "Item clicked is : " + dataModal.CategoryName, Toast.LENGTH_SHORT).show()
            }
        }
        return listitemView
    }
}

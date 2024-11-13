package com.example.a14_firebaseaccess.entities

import android.os.Parcel
import android.os.Parcelable

data class cls_Product(
    val ProductID: String? = null,
    val ProductName: String? = null,
    val SupplierID: String? = null,
    val CategoryID: String? = null,
    var QuantityPerUnit: Double = 1.0,
    val UnitPrice: Double? = 0.0,
    val UnitsInStock: String? = null,
    val UnitsOnOrder: String? = null,
    val ReorderLevel: String? = null,
    val Discontinued: String? = null,
    var Discount: Double? = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ProductID)
        parcel.writeString(ProductName)
        parcel.writeString(SupplierID)
        parcel.writeString(CategoryID)
        parcel.writeDouble(QuantityPerUnit)
        parcel.writeValue(UnitPrice)
        parcel.writeString(UnitsInStock)
        parcel.writeString(UnitsOnOrder)
        parcel.writeString(ReorderLevel)
        parcel.writeString(Discontinued)
        parcel.writeValue(Discount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<cls_Product> {
        override fun createFromParcel(parcel: Parcel): cls_Product {
            return cls_Product(parcel)
        }

        override fun newArray(size: Int): Array<cls_Product?> {
            return arrayOfNulls(size)
        }
    }
}

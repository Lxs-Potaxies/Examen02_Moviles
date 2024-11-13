package com.example.a14_firebaseaccess.database

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import com.example.a14_firebaseaccess.entities.cls_Product

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "productos.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_PRODUCTS = "products"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PRODUCT_ID = "product_id"
        private const val COLUMN_PRODUCT_NAME = "product_name"
        private const val COLUMN_UNIT_PRICE = "unit_price"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_DISCOUNT = "discount"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PRODUCTS_TABLE = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PRODUCT_ID TEXT,
                $COLUMN_PRODUCT_NAME TEXT,
                $COLUMN_UNIT_PRICE REAL,
                $COLUMN_QUANTITY REAL,
                $COLUMN_DISCOUNT REAL
            );
        """
        db?.execSQL(CREATE_PRODUCTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }

    fun addProduct(product: cls_Product) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_PRODUCT_ID, product.ProductID)
        values.put(COLUMN_PRODUCT_NAME, product.ProductName)
        values.put(COLUMN_UNIT_PRICE, product.UnitPrice)
        values.put(COLUMN_QUANTITY, product.QuantityPerUnit)
        values.put(COLUMN_DISCOUNT, product.Discount)

        db.insert(TABLE_PRODUCTS, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getAllProducts(): List<cls_Product> {
        val productList = mutableListOf<cls_Product>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PRODUCTS", null)

        if (cursor.moveToFirst()) {
            do {
                val product = cls_Product(
                    ProductID = cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_ID)),
                    ProductName = cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_NAME)),
                    UnitPrice = cursor.getDouble(cursor.getColumnIndex(COLUMN_UNIT_PRICE)),
                    QuantityPerUnit = cursor.getDouble(cursor.getColumnIndex(COLUMN_QUANTITY)),
                    Discount = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISCOUNT))
                )
                productList.add(product)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return productList
    }

    fun deleteAllProducts() {
        val db = this.writableDatabase
        db.delete(TABLE_PRODUCTS, null, null)
        db.close()
    }
}

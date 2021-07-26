package com.koombea.couchbaselitewrapper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.koombea.couchbaselitewrapper.databinding.ActivityMainBinding
import com.koombea.couchbaselitewrapper.model.Product
import com.kouchbase.couchbasewrapper.database.CouchbaseDatabase
import com.kouchbase.couchbasewrapper.database.CouchbaseDocument

class MainActivity : AppCompatActivity() {

    private lateinit var productDatabase: CouchbaseDatabase
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        productDatabase = CouchbaseDatabase(this, "products_db")
        setup()
    }

    private fun setup() {
        printAllProducts()
        binding.saveProductButton.setOnClickListener {
            saveProduct()
            printAllProducts()
            clearFields()
        }
        binding.deleteProductButton.setOnClickListener {
            deleteProduct()
            printAllProducts()
        }
        binding.deleteAllButton.setOnClickListener {
            deleteAllProducts()
            printAllProducts()
        }
        binding.quantityLimitButton.setOnClickListener {
            printAllWhereQuantityIsLessThanOrEqualTo(binding.quantityLimitFilterEditText.text.toString().toIntOrNull() ?: 0)
        }
        binding.sortDescendingByQuantityButton.setOnClickListener {
            printAllSortedByQuantityDescending()
        }
        binding.sortAscendingByQuantityButton.setOnClickListener {
            printAllSortedByQuantityAscending()
        }
    }

    private fun saveProduct() {
        val id = binding.idEditText.text.toString()
        val name = binding.nameEditText.text.toString()
        val quantity = binding.quantityEditText.text.toString().toIntOrNull() ?: 0
        val product = Product(
            id = id,
            name = name,
            quantity = quantity
        )
        val document =
            CouchbaseDocument(id = id, attributes = product)
        productDatabase.save(document)
    }

    private fun deleteProduct() {
        val id = binding.idEditText.text.toString()
        productDatabase.delete(id)
    }

    private fun deleteAllProducts() {
        productDatabase.deleteAll()
    }

    private fun printAllWhereQuantityIsLessThanOrEqualTo(lessThan: Int) {
        val whereExpression = Expression.property("attributes.quantity").lessThanOrEqualTo(Expression.intValue(lessThan))
        val productList = productDatabase.fetchAll<Product>(whereExpression = whereExpression)
        printList(productList)
    }

    private fun printAllSortedByQuantityDescending() {
        val orderBy = arrayOf(Ordering.property("attributes.quantity").descending())
        val productList = productDatabase.fetchAll<Product>(orderedBy = orderBy)
        printList(productList)
    }

    private fun printAllSortedByQuantityAscending() {
        val orderBy = arrayOf(Ordering.property("attributes.quantity").ascending())
        val productList = productDatabase.fetchAll<Product>(orderedBy = orderBy)
        printList(productList)
    }

    private fun printAllProducts() {
        val productList = productDatabase.fetchAll<Product>()
        printList(productList)
    }

    private fun printList(productList: List<Product>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, productList)
        binding.productsListView.adapter = adapter
    }

    private fun clearFields() {
        binding.idEditText.text?.clear()
        binding.nameEditText.text?.clear()
        binding.quantityEditText.text?.clear()
    }
}
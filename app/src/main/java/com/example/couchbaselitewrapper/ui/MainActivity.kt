//
// Copyright (c) 2021 Koombea, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.example.couchbaselitewrapper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.example.couchbaselitewrapper.databinding.ActivityMainBinding
import com.example.couchbaselitewrapper.model.Product
import com.koombea.couchbasewrapper.database.CouchbaseDatabase
import com.koombea.couchbasewrapper.database.CouchbaseDocument

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
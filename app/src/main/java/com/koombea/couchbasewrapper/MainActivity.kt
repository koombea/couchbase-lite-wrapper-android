package com.koombea.couchbasewrapper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.koombea.couchbasewrapper.database.CouchbaseDatabase
import com.koombea.couchbasewrapper.database.CouchbaseDocument
import com.koombea.couchbasewrapper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var productDatabase: CouchbaseDatabase
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        productDatabase = CouchbaseDatabase(this, "products_db")
//        val product1 = Product(id = "1", "car", 1000)
//        val product2 = Product(id = "2", "plane", 2000)
//
//        productDatabase = CouchBaseDatabase(this, "products_db")
//        productDatabase.save(CouchBaseDocument("1", product1))
//        productDatabase.save(CouchBaseDocument("2", product2))
//
//        val fetchAllResult: List<Product> = productDatabase.fetchAll(
//            modelType = Product::class.java)
//        Log.d(TAG, "onCreate: fetchAllResult: $fetchAllResult")
//
//        val fetchAllResultWhere1: List<Product> = productDatabase.fetchAll(
//            modelType = Product::class.java,
//            whereExpression = Expression.property("attributes.name").equalTo(Expression.string("car")))
//        Log.d(TAG, "onCreate: fetchAllResultWhere name=car : $fetchAllResultWhere1")
//
//        val fetchAllResultWhere2: List<Product> = productDatabase.fetchAll(
//            modelType = Product::class.java,
//            whereExpression = Expression.property("attributes.price").equalTo(Expression.intValue(2000)))
//        Log.d(TAG, "onCreate: fetchAllResultWhere price=2000 : $fetchAllResultWhere2")
//        productDatabase.deleteAll(modelType = Product::class.java)
//        val fetchAllProductsAfterDeleteAll: List<Product> = productDatabase.fetchAll(
//            modelType = Product::class.java)
//        Log.d(TAG, "onCreate: fetchAllProductsAfterDeleteAll: $fetchAllProductsAfterDeleteAll")
//
//        val person1 = Person("Juan", 22)
//        val person2 = Person("Gabriel", 18)
//        val personList = listOf(
//            CouchBaseDocument(id = "1", attributes = person1),
//            CouchBaseDocument(id = "2", attributes = person2)
//        )
//        val personDatabase = CouchBaseDatabase(this, "persons_db")
//        personDatabase.save(personList)
//        val fetchAllPersons = personDatabase.fetchAll(modelType = Person::class.java)
//        Log.d(TAG, "onCreate: fetchAllPersons: $fetchAllPersons")
//        val fetchPerson2: List<Person> = personDatabase.fetch(CouchBaseDocument(id = "2", attributes = person2), Person::class.java)
//        Log.d(TAG, "onCreate: fetchPerson2: $fetchPerson2")
//        val fetchPerson3: List<Person> = personDatabase.fetch(CouchBaseDocument(id = "3", attributes = person2), Person::class.java)
//        Log.d(TAG, "onCreate: fetchPerson3: $fetchPerson3")
//
//        val animalDatabase = CouchBaseDatabase(this, "animals_db")
//        val fetchAllAnimals = animalDatabase.fetchAll(modelType = Animal::class.java)
//        Log.d(TAG, "onCreate: fetchAllAnimals: $fetchAllAnimals")
//        val animal1 = CouchBaseDocument("1", attributes = Animal(
//            identifier = "1",
//            name = "dog",
//            legs = 4
//        ))
//        val animal2 = CouchBaseDocument("2", attributes = Animal(
//            identifier = "2",
//            name = "cat",
//            legs = 4
//        ))
//        val animal3 = CouchBaseDocument("3", attributes = Animal(
//            identifier = "3",
//            name = "fish",
//            legs = 0
//        ))
//        val animal4 = CouchBaseDocument("4", attributes = Animal(
//            identifier = "4",
//            name = "monkey",
//            legs = 2
//        ))
//        val animalList = listOf(animal1, animal2, animal3, animal4)
//        animalDatabase.save(animalList)
//        val fetchAllAnimalsAfterSave = animalDatabase.fetchAll(modelType = Animal::class.java)
//        Log.d(TAG, "onCreate: fetchAllAnimalsAfterSave: $fetchAllAnimalsAfterSave")
//        animalDatabase.delete(animal1)
//        val fetchAllAnimalsAfterDeleteFirst = animalDatabase.fetchAll(modelType = Animal::class.java)
//        Log.d(TAG, "onCreate: fetchAllAnimalsAfterDeleteFirst: $fetchAllAnimalsAfterDeleteFirst")
//        animalDatabase.delete(listOf(animal3, animal4))
//        val fetchAllAnimalsAfterDeleteLastTwo = animalDatabase.fetchAll(modelType = Animal::class.java)
//        Log.d(TAG, "onCreate: fetchAllAnimalsAfterDeleteLastTwo: $fetchAllAnimalsAfterDeleteLastTwo")
//        animalDatabase.delete(listOf(animal3, animal4))
//        val fetchAllAnimalsAfterTryDeleteAlreadyDeleted = animalDatabase.fetchAll(modelType = Animal::class.java)
//        Log.d(TAG, "onCreate: fetchAllAnimalsAfterTryDeleteAlreadyDeleted: $fetchAllAnimalsAfterTryDeleteAlreadyDeleted")
//        animalDatabase.save(animalList)
//        val fetchAllAnimalsAfterSaveAll= animalDatabase.fetchAll(modelType = Animal::class.java)
//        Log.d(TAG, "onCreate: fetchAllAnimalsAfterSaveAll: $fetchAllAnimalsAfterSaveAll")
//        animalDatabase.deleteAll(whereExpression = Expression.property("attributes.identifier").equalTo(
//            Expression.string("2")), modelType = Animal::class.java)
//        val fetchAllAnimalsAfterDeleteWhere= animalDatabase.fetchAll(modelType = Animal::class.java)
//        Log.d(TAG, "onCreate: fetchAllAnimalsAfterDeleteWhere id=2: $fetchAllAnimalsAfterDeleteWhere")
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
        val document = CouchbaseDocument(id = id, attributes = product)
        productDatabase.save(document)
    }

    private fun deleteProduct() {
        val id = binding.idEditText.text.toString()
        productDatabase.delete(id)
    }

    private fun deleteAllProducts() {
        productDatabase.deleteAll(modelType = Product::class.java)
    }

    private fun printAllWhereQuantityIsLessThanOrEqualTo(lessThan: Int) {
        val whereExpression = Expression.property("attributes.quantity").lessThanOrEqualTo(Expression.intValue(lessThan))
        val productList = productDatabase.fetchAll(whereExpression = whereExpression, modelType = Product::class.java)
        printList(productList)
    }

    private fun printAllSortedByQuantityDescending() {
        val orderBy = arrayOf(Ordering.property("attributes.quantity").descending())
        val productList = productDatabase.fetchAll(orderedBy = orderBy, modelType = Product::class.java)
        printList(productList)
    }

    private fun printAllSortedByQuantityAscending() {
        val orderBy = arrayOf(Ordering.property("attributes.quantity").ascending())
        val productList = productDatabase.fetchAll(orderedBy = orderBy, modelType = Product::class.java)
        printList(productList)
    }

    private fun printAllProducts() {
        val productList = productDatabase.fetchAll(modelType = Product::class.java)
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

    companion object {
        data class Person(
            val name: String,
            val age: Int
        )
        data class Animal(
            val identifier: String,
            val name: String,
            val legs: Int
        )
        private const val TAG = "DEBUG"
    }
}
package com.koombea.couchbasewrapper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.couchbase.lite.Expression
import com.koombea.couchbasewrapper.database.CouchBaseDatabase
import com.koombea.couchbasewrapper.database.CouchBaseDocument
import com.koombea.couchbasewrapper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var productDatabase: CouchBaseDatabase
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        productDatabase = CouchBaseDatabase(this, "products_db")
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
        binding.saveProductsButton.setOnClickListener {
            saveProducts()
        }
        binding.fetchAllProductsButton.setOnClickListener {
            printAllProducts()
        }
    }

    private fun saveProducts() {
        val product1 = Product(id = "1", "car", 1000)
        val product2 = Product(id = "2", "plane", 2000)
//        val product1 = Product(id = "1", "car")
//        val product2 = Product(id = "2", "plane")
        val productDocument1 = CouchBaseDocument(id = product1.id, attributes = product1)
        val productDocument2 = CouchBaseDocument(id = product2.id, attributes = product2)
        val productDocumentList = listOf(productDocument1, productDocument2)
        productDatabase.save(productDocumentList)
    }

    private fun printAllProducts() {
        val fetchAll = productDatabase.fetchAll(modelType = Product::class.java)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fetchAll)
        binding.productsListView.adapter = adapter
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
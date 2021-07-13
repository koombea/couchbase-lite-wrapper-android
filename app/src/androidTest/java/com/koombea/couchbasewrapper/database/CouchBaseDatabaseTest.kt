package com.koombea.couchbasewrapper.database

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule

@SmallTest
@ExperimentalCoroutinesApi
class CouchBaseDatabaseTest {

    lateinit var database: CouchBaseDatabase
    lateinit var instrumentationContext: Context
    private val product1 = Product(id = "1", name = "car", 40)
    private val product2 = Product(id = "2", name = "plane", 20)
    private val product3 = Product(id = "3", name = "skate", 30)
    private val product4 = Product(id = "4", name = "boat", 10)
    private val document1 = CouchBaseDocument(id = product1.id, attributes = product1)
    private val document2 = CouchBaseDocument(id = product2.id, attributes = product2)
    private val document3 = CouchBaseDocument(id = product3.id, attributes = product3)
    private val document4 = CouchBaseDocument(id = product4.id, attributes = product4)
    private val products = listOf(product1, product2, product3, product4)
    private val documents = listOf(document1, document2, document3, document4)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() = runBlockingTest {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        database = CouchBaseDatabase(
                    instrumentationContext,
            "TEST_DB")
        //awaitInitialization()
    }

    @Test
    fun saveAndFetchAll() = runBlockingTest {
        database.save(documents)
        val fetchAll = database.fetchAll(modelType = Product::class.java)
        assertEquals(products, fetchAll)
    }

    @Test
    fun fetchOnlyOneObject() = runBlockingTest {
        database.save(documents)
        val fetchOne = database.fetch(document = document1, modelType = Product::class.java).first()
        assertEquals(product1, fetchOne)
    }

    @Test
    fun fetchOnlyOneObjectWhereIdEqualsTwo() = runBlockingTest {
        database.save(documents)
        val whereExpression = Expression.property("attributes.id").equalTo(Expression.string("2"))
        val fetchOne = database.fetchAll(whereExpression = whereExpression, modelType = Product::class.java).first()
        assertEquals(product2, fetchOne)
    }

    @Test
    fun fetchAllOrderedByQuantityDescending() = runBlockingTest {
        database.save(documents)
        val productsOrderedDescendingByQuantity = listOf(product1, product3, product2, product4)
        val orderBy = arrayOf(Ordering.property("attributes.quantity").descending())
        val fetchAll = database.fetchAll(orderedBy = orderBy, modelType = Product::class.java)
        assertEquals(productsOrderedDescendingByQuantity, fetchAll)
    }

    @Test
    fun fetchAllWhereIdIsGreaterThanTwoOrderedByQuantityAscending() = runBlockingTest {
        database.save(documents)
        val expected = listOf(product4, product3)
        val whereExpression = Expression.property("attributes.id").greaterThan(Expression.string("2"))
        val orderBy = arrayOf(Ordering.property("attributes.quantity").ascending())
        val fetchAll = database.fetchAll(whereExpression = whereExpression, orderedBy = orderBy, modelType = Product::class.java)
        assertEquals(expected, fetchAll)
    }

    @Test
    fun updateADocumentWithSaveAndUpdatePolicy() = runBlockingTest {
        database.save(documents)
        val product = Product(id = "1", name = "new product", quantity = 999)
        val expected = products.subList(1, products.size).toMutableList()
        expected.add(0, product)
        database.save(CouchBaseDocument(id = "1", attributes = product))
        val fetchAll = database.fetchAll(modelType = Product::class.java)
        assertEquals(expected, fetchAll)
    }

    private suspend fun awaitInitialization() {
        while(!this::database.isInitialized) {
            delay(100)
        }
        println("Database initialized")
    }
    @After
    fun teardown() {
        database.deleteDatabase()
    }
}
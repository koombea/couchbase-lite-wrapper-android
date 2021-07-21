package com.koombea.couchbasewrapper.database

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.koombea.couchbasewrapper.Product
import com.koombea.couchbasewrapper.ShoppingCart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import kotlin.coroutines.CoroutineContext

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
    private val shoppingCart1 = ShoppingCart(id = "1", date = "05-05-2020", product = product1)
    private val shoppingCart2 = ShoppingCart(id = "2", date = "15-06-2020", product = product2)
    private val shoppingCart3 = ShoppingCart(id = "3", date = "25-07-2020", product = product3)
    private val shoppingCart4 = ShoppingCart(id = "4", date = "05-08-2020", product = product4)
    private val shoppingCartDocument1 = CouchBaseDocument(id = shoppingCart1.id, attributes = shoppingCart1)
    private val shoppingCartDocument2 = CouchBaseDocument(id = shoppingCart2.id, attributes = shoppingCart2)
    private val shoppingCartDocument3 = CouchBaseDocument(id = shoppingCart3.id, attributes = shoppingCart3)
    private val shoppingCartDocument4 = CouchBaseDocument(id = shoppingCart4.id, attributes = shoppingCart4)
    private val shoppingCarts = listOf(shoppingCart1, shoppingCart2, shoppingCart3, shoppingCart4)
    private val shoppingCartDocuments = listOf(shoppingCartDocument1, shoppingCartDocument2, shoppingCartDocument3, shoppingCartDocument4)

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private val networkContext: CoroutineContext = testDispatcher

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
        val fetchAll = database.fetchAll<Product>()
        assertEquals(products, fetchAll)
    }

    @Test
    fun saveAndFetchAllWithNestedObject() = runBlockingTest {
        database.save(shoppingCartDocuments)
        val fetchAll: List<ShoppingCart> = database.fetchAll()
        assertEquals(shoppingCarts, fetchAll)
    }

    @Test
    fun fetchOnlyOneObject() = runBlockingTest {
        database.save(documents)
        //val fetchOne = database.fetch(document = document1, modelType = Product::class.java).first()
        val fetchOne = database.find<Product>("1")
        assertEquals(product1, fetchOne)
    }

    @Test
    fun fetchOnlyOneObjectWhereIdEqualsTwo() = runBlockingTest {
        database.save(documents)
        val whereExpression = Expression.property("attributes.id").equalTo(Expression.string("2"))
        val fetchOne: Product = database.fetchAll<Product>(whereExpression = whereExpression).first()
        assertEquals(product2, fetchOne)
    }

    @Test
    fun fetchAllOrderedByQuantityDescending() = runBlockingTest {
        database.save(documents)
        val productsOrderedDescendingByQuantity = listOf(product1, product3, product2, product4)
        val orderBy = arrayOf(Ordering.property("attributes.quantity").descending())
        val fetchAll: List<Product> = database.fetchAll(orderedBy = orderBy)
        assertEquals(productsOrderedDescendingByQuantity, fetchAll)
    }

    @Test
    fun fetchAllWhereIdIsGreaterThanTwoOrderedByQuantityAscending() = runBlockingTest {
        database.save(documents)
        val expected = listOf(product4, product3)
        val whereExpression = Expression.property("attributes.id").greaterThan(Expression.string("2"))
        val orderBy = arrayOf(Ordering.property("attributes.quantity").ascending())
        val fetchAll: List<Product> = database.fetchAll(whereExpression = whereExpression, orderedBy = orderBy)
        assertEquals(expected, fetchAll)
    }

    @Test
    fun updateADocumentWithSaveAndUpdatePolicy() = runBlockingTest {
        database.save(documents)
        val product = Product(id = "1", name = "new product", quantity = 999)
        val expected = products.subList(1, products.size).toMutableList()
        expected.add(0, product)
        database.save(CouchBaseDocument(id = "1", attributes = product))
        val fetchAll: List<Product> = database.fetchAll()
        assertEquals(expected, fetchAll)
    }

    @Test
    fun deleteAllDocuments() = runBlockingTest {
        database.save(documents)
        database.deleteAll()
        val fetchAll: List<Product> = database.fetchAll()
        assertEquals(0, fetchAll.size)
    }

    @Test
    fun deleteAllWhereQuantityIsGreaterThan20() = runBlockingTest {
        database.save(documents)
        val whereExpression = Expression.property("attributes.quantity").greaterThan(Expression.intValue(20))
        database.deleteAll(whereExpression = whereExpression)
        val fetchAll: List<Product> = database.fetchAll()
        val expected = products.filterNot { it.quantity!! > 20 }
        assertEquals(expected, fetchAll)
    }

    @Test
    fun deleteOnlyTheFirstDocument() = runBlockingTest {
        database.save(documents)
        database.delete(CouchBaseDocument(id = "1", attributes = product1))
        val fetchAll: List<Product> = database.fetchAll()
        val expected = products.subList(1, products.size)
        assertEquals(expected, fetchAll)
    }

    @Test
    fun deleteTheFirstTwoDocuments() = runBlockingTest {
        database.save(documents)
        database.delete(listOf(
            CouchBaseDocument(id = "1", attributes = product1),
            CouchBaseDocument(id = "2", attributes = product2))
        )
        val fetchAll: List<Product> = database.fetchAll()
        val expected = products.subList(2, products.size)
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
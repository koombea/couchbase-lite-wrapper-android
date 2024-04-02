package com.koombea.couchbasewrapper

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering
import com.example.couchbaselitewrapper.model.Product
import com.example.couchbaselitewrapper.model.ShoppingCart
import com.koombea.couchbasewrapper.database.CouchbaseCollection
import com.koombea.couchbasewrapper.database.CouchbaseDatabase
import com.koombea.couchbasewrapper.database.CouchbaseDocument
import com.koombea.couchbasewrapper.utils.CustomExpression
import com.koombea.couchbasewrapper.utils.InvalidNameIndexException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@SmallTest
@ExperimentalCoroutinesApi
class CouchBaseCollectionTest {

    private lateinit var database: CouchbaseDatabase
    private lateinit var collection: CouchbaseCollection
    private lateinit var instrumentationContext: Context
    private val product1 = Product(id = "1", name = "car", 40)
    private val product2 = Product(id = "2", name = "plane", 20)
    private val product3 = Product(id = "3", name = "skate", 30)
    private val product4 = Product(id = "4", name = "boat", 10)
    private val document1 =
        CouchbaseDocument(id = product1.id, attributes = product1)
    private val document2 =
        CouchbaseDocument(id = product2.id, attributes = product2)
    private val document3 =
        CouchbaseDocument(id = product3.id, attributes = product3)
    private val document4 =
        CouchbaseDocument(id = product4.id, attributes = product4)
    private val products = listOf(product1, product2, product3, product4)
    private val documents = listOf(document1, document2, document3, document4)
    private val shoppingCart1 = ShoppingCart(id = "1", date = "05-05-2020", product = product1)
    private val shoppingCart2 = ShoppingCart(id = "2", date = "15-06-2020", product = product2)
    private val shoppingCart3 = ShoppingCart(id = "3", date = "25-07-2020", product = product3)
    private val shoppingCart4 = ShoppingCart(id = "4", date = "05-08-2020", product = product4)
    private val shoppingCartDocument1 = CouchbaseDocument(
        id = shoppingCart1.id,
        attributes = shoppingCart1
    )
    private val shoppingCartDocument2 = CouchbaseDocument(
        id = shoppingCart2.id,
        attributes = shoppingCart2
    )
    private val shoppingCartDocument3 = CouchbaseDocument(
        id = shoppingCart3.id,
        attributes = shoppingCart3
    )
    private val shoppingCartDocument4 = CouchbaseDocument(
        id = shoppingCart4.id,
        attributes = shoppingCart4
    )
    private val shoppingCarts = listOf(shoppingCart1, shoppingCart2, shoppingCart3, shoppingCart4)
    private val shoppingCartDocuments = listOf(shoppingCartDocument1, shoppingCartDocument2, shoppingCartDocument3, shoppingCartDocument4)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        database = CouchbaseDatabase(
            instrumentationContext,
            "TEST_DB"
        )

        database.createCollection("Vehicle")

        collection = CouchbaseCollection(
            couchbaseDatabase = database,
            collection = database.getCollection("Vehicle")!!
        )
    }

    @Test
    fun saveAndFetchAll() = runTest {
        collection.save(documents)
        val fetchAll = collection.fetchAll<Product>()
        Assert.assertEquals(products, fetchAll)
    }

    @Test
    fun saveAndFetchAllWithNestedObject() = runTest {
        collection.save(shoppingCartDocuments)
        val fetchAll = collection.fetchAll<ShoppingCart>()
        Assert.assertEquals(shoppingCarts, fetchAll)
    }

    @Test
    fun saveAndFetchDouble() = runTest {
        val expected = 20.5
        collection.save(
            CouchbaseDocument(
                id = "1",
                attributes = expected
            )
        )
        val fetch = collection.fetchAll<Double>().first()
        Assert.assertEquals(expected, fetch, 0.001)
    }

    @Test
    fun fetchOnlyOneObject() = runTest {
        collection.save(document1)
        val fetchOne = collection.fetch<Product>(id = document1.id)
        Assert.assertEquals(product1, fetchOne)
    }

    @Test
    fun fetchOnlyOneObjectWhereIdEqualsTwo() = runTest {
        collection.save(documents)
        val whereExpression = Expression.property("attributes.id").equalTo(Expression.string("2"))
        val fetchOne = collection.fetchAll<Product>(whereExpression = whereExpression).first()
        Assert.assertEquals(product2, fetchOne)
    }

    @Test
    fun fetchOnlyOneObjectWhereIdEqualsTwoWithAttributesPrepend() = runTest {
        collection.save(documents)
        val whereExpression = CustomExpression.property("id").equalTo(Expression.string("2"))
        val fetchOne = collection.fetchAll<Product>(whereExpression = whereExpression).first()
        Assert.assertEquals(product2, fetchOne)
    }

    @Test
    fun fetchAllOrderedByQuantityDescending() = runTest {
        collection.save(documents)
        val productsOrderedDescendingByQuantity = listOf(product1, product3, product2, product4)
        val orderBy = arrayOf(Ordering.property("attributes.quantity").descending())
        val fetchAll = collection.fetchAll<Product>(orderedBy = orderBy)
        Assert.assertEquals(productsOrderedDescendingByQuantity, fetchAll)
    }

    @Test
    fun fetchAllWhereIdIsGreaterThanTwoOrderedByQuantityAscending() = runTest {
        collection.save(documents)
        val expected = listOf(product4, product3)
        val whereExpression = Expression.property("attributes.id").greaterThan(Expression.string("2"))
        val orderBy = arrayOf(Ordering.property("attributes.quantity").ascending())
        val fetchAll = collection.fetchAll<Product>(whereExpression = whereExpression, orderedBy = orderBy)
        Assert.assertEquals(expected, fetchAll)
    }

    @Test
    fun updateADocumentWithSaveAndUpdatePolicy() = runTest {
        collection.save(documents)
        val product = Product(id = "1", name = "new product", quantity = 999)
        val expected = products.subList(1, products.size).toMutableList()
        expected.add(0, product)
        collection.save(
            CouchbaseDocument(
                id = "1",
                attributes = product
            )
        )
        val fetchAll = collection.fetchAll<Product>()
        Assert.assertEquals(expected, fetchAll)
    }

    @Test
    fun deleteAllDocuments() = runTest {
        collection.save(documents)
        collection.deleteAll()
        val fetchAll = collection.fetchAll<Product>()
        Assert.assertEquals(0, fetchAll.size)
    }

    @Test
    fun deleteAllWhereQuantityIsGreaterThan20() = runTest {
        collection.save(documents)
        val whereExpression = Expression.property("attributes.quantity").greaterThan(Expression.intValue(20))
        collection.deleteAll(whereExpression = whereExpression)
        val fetchAll = collection.fetchAll<Product>()
        val expected = products.filterNot { it.quantity > 20 }
        Assert.assertEquals(expected, fetchAll)
    }

    @Test
    fun deleteOnlyTheFirstDocument() = runTest {
        collection.save(documents)
        collection.delete(id = "1")
        val fetchAll = collection.fetchAll<Product>()
        val expected = products.subList(1, products.size)
        Assert.assertEquals(expected, fetchAll)
    }

    @Test
    fun whenCreateIndexIndexCreatedSuccessfully() = runTest {
        collection.createIndex("TestIndex", listOf("type", "name"))
    }

    @Test(expected = InvalidNameIndexException::class)
    fun whenCreateIndexWithTheWordIndexThrowsAnException() = runTest {
        collection.createIndex("index", listOf("type", "name"))
    }

    @After
    fun teardown() {
        database.deleteDatabase()
    }
}
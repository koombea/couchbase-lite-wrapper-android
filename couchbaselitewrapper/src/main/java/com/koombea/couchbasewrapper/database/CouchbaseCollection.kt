package com.koombea.couchbasewrapper.database

import com.couchbase.lite.Collection
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.From
import com.couchbase.lite.IndexBuilder
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Ordering
import com.couchbase.lite.Query
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.couchbase.lite.ValueIndexItem
import com.couchbase.lite.Where
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koombea.couchbasewrapper.utils.InvalidNameIndexException
import java.lang.Exception
import java.util.HashMap

class CouchbaseCollection(
    @PublishedApi internal val couchbaseDatabase: CouchbaseDatabase,
    @PublishedApi internal val collection: Collection
) {

    val database = couchbaseDatabase.database

    @PublishedApi
    internal val gson: Gson by lazy { Gson() }

    /**
     * Saves a document into a Collection
     * @param document [CouchbaseDocument] to be saved
     */
    fun <T> save(document: CouchbaseDocument<T>) {
        val mutableDocument =
            collection.getDocument(document.id)?.toMutable() ?: MutableDocument(document.id)
        try {
            val data: HashMap<String, Any> =
                gson.fromJson(
                    gson.toJson(document),
                    object : TypeToken<HashMap<String, Any>>() {}.type
                )
            mutableDocument.apply {
                setValue(document.id, data)
            }
            collection.save(MutableDocument(document.id, data))
       } catch (e: Exception) {
            e.printStackTrace()
       }
    }

    /**
     * Save a list of documents in the collection
     * @param documents list of documents to be saved
     */
    fun <T> save(documents: List<CouchbaseDocument<T>>) {
        database.inBatch<CouchbaseLiteException> {
            documents.forEach { document ->
                save(document)
            }
        }
    }

    /**
     * Add an index to the collection.
     *  @param indexName The name identifier to the collection.
     *  @param properties list of the properties or expressions to be indexed.
     *  @throws InvalidNameIndexException If the name is the word Index or index.
     */
    @Throws(InvalidNameIndexException::class)
    fun createIndex(indexName: String, properties: List<String> = emptyList()) {
        if(indexName.equals("index", true)) {
            throw InvalidNameIndexException()
        }
        val valueIndex = properties.map {
            ValueIndexItem.property(it)
        }
        collection.createIndex(indexName,
            IndexBuilder.valueIndex(
                *valueIndex.toTypedArray()
            ))
    }

    /**
     * Fetch all data in the collection
     * @param whereExpression expression to filter the query
     * @param orderedBy array of Ordering objects, which are useful to sort the retrieved data
     * @return list of generic object
     */
   inline fun <reified T> fetchAll(
        whereExpression: Expression? = null,
        orderedBy: Array<Ordering>? = null
    ): List<T> {
        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(this.collection))

        (query as From).let { fromQuery ->
            if (whereExpression != null) {
                query = fromQuery.where(whereExpression)
                (query as Where).let { whereQuery ->
                    if (orderedBy != null) {
                        query = whereQuery.orderBy(*orderedBy)
                    }
                }
            } else if (orderedBy != null) {
                query = fromQuery.orderBy(*orderedBy)
            }
        }
        val results = mutableListOf<T>()

        database.inBatch<CouchbaseLiteException> {
            query.execute().forEach { result ->
                result.toMap()[collection.name]?.let { document ->
                    val data = mapObjectToCouchBaseDocument<T>(document)
                    results.add(data.attributes)
                }
            }
        }
        return results
    }

    /**
     * Fetch one document based on the id of the input document.
     * @param id id of the document to be retrieved
     * @return list of generic object with size 1 if the searched object was found, otherwise size 0
     */
    inline fun <reified T> fetch(id: String): T? {
        val document = collection.getDocument(id)
        if(document != null) {
            return mapObjectToCouchBaseDocument<T>(document.toMap()).attributes
        }
        return null
    }

    /**
     * Delete all documents in the collection
     * @param whereExpression expression to filter the documents to be deleted
     */
    fun deleteAll(whereExpression: Expression? = null) {
        val fromQuery = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))
        var whereQuery: Where? = null
        if(whereExpression != null) {
            whereQuery = fromQuery.where(whereExpression)
        }
        val query = whereQuery ?: fromQuery
        try {
            database.inBatch<CouchbaseLiteException> {
                (query as Query).execute().forEach { result ->
                    result.toMap()[collection.name]?.let { document ->
                        val type = object : TypeToken<CouchbaseDocument<Any>>() {}.type
                        val couchbaseDocument = gson.fromJson<CouchbaseDocument<Any>>(gson.toJson(document), type)
                        collection.getDocument(couchbaseDocument.id)?.let { collection.delete(it) }
                    }
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete one document of the collection
     * @param id id of the document to be deleted
     */
    fun delete(id: String) {
        val databaseDocument = collection.getDocument(id) ?: return
        try {
            collection.delete(databaseDocument)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete a list of documents of the collection
     * @param idList list of ids of documents to be deleted
     */
    @JvmName("delete1")
    fun delete(idList: List<String>) {
        try {
            database.inBatch<CouchbaseLiteException> {
                idList.forEach { id ->
                    delete(id)
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @PublishedApi
    internal inline fun <reified T> mapObjectToCouchBaseDocument(document: Any): CouchbaseDocument<T> {
        val type = object : TypeToken<CouchbaseDocument<T>>() {}.type
        return gson.fromJson(gson.toJson(document), type)
    }
}

package com.koombea.couchbasewrapper.database

import android.content.Context
import android.util.Log
import com.couchbase.lite.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koombea.couchbasewrapper.database.CouchBaseSavePolicy.SAVE_AND_UPDATE
import java.lang.Exception
import java.util.*

/**
 * Couchbase Wrapper with basic CRUD
 * The intended usage is to save only one type of object in each database instance
 */
class CouchBaseDatabase (
    private val context: Context,
    private val databaseName: String,
    private val configuration: CouchBaseDatabaseConfiguration? = null
) {

    private val gson: Gson by lazy { Gson() }

    init {
        setup()
    }

    private lateinit var database: Database

    private fun setup() {
        kotlin.runCatching {
            CouchbaseLite.init(context)
        }.onSuccess {
            val databaseConfiguration = DatabaseConfiguration()
            configuration?.databaseDirectory?.let { directory ->
                databaseConfiguration.directory = directory
            }
            configuration?.let {
                database = Database(
                    databaseName,
                    databaseConfiguration
                )
            } ?: run {
                database = Database(
                    databaseName
                )
            }
        }
    }

    private fun <T> fetchQuery(query: Query, modelType: Class<T>): List<T> {
        val results = mutableListOf<T>()
        database.inBatch {
            query.execute().forEach { result ->
                result.toMap()[databaseName]?.let { document ->
                    val data = mapObjectToCouchBaseDocument(document, modelType)
                    results.add(data.attributes)
                    //Log.d(TAG, "fetchQuery: data: $data")
                    //Log.d(TAG, "fetchQuery: attributes: ${data.attributes}")
                }
            }
        }
        return results
    }

    private fun <T> mapObjectToCouchBaseDocument(document: Any, modelType: Class<T>): CouchBaseDocument<T> {
        val type = when (modelType) {
            Boolean::class.java -> object : TypeToken<CouchBaseDocument<Boolean>>() {}.type
            Char::class.java -> object : TypeToken<CouchBaseDocument<Char>>() {}.type
            Byte::class.java -> object : TypeToken<CouchBaseDocument<Byte>>() {}.type
            Short::class.java -> object : TypeToken<CouchBaseDocument<Short>>() {}.type
            Int::class.java -> object : TypeToken<CouchBaseDocument<Int>>() {}.type
            Long::class.java -> object : TypeToken<CouchBaseDocument<Long>>() {}.type
            Float::class.java -> object : TypeToken<CouchBaseDocument<Float>>() {}.type
            Double::class.java -> object : TypeToken<CouchBaseDocument<Double>>() {}.type
            Unit::class.java -> object : TypeToken<CouchBaseDocument<Unit>>() {}.type
            Void::class.java -> object : TypeToken<CouchBaseDocument<Void>>() {}.type
            else -> TypeToken.getParameterized(CouchBaseDocument::class.java, modelType).type
        }
        //Log.d(TAG, "mapObjectToCouchBaseDocument: document: $document")
        //Log.d(TAG, "mapObjectToCouchBaseDocument: modelType: $modelType")
        return gson.fromJson(gson.toJson(document), type)
    }

    /**
     * Save a document in the database
     * @param document document to be saved
     * @param policy save policy, if SAVE_AND_UPDATE, documents with the same id will be overwritten
     * if CREATE_NEW_ALWAYS, documents with the same id will be created as new documents in the database
     */
    fun <T> save(document: CouchBaseDocument<T>, policy: CouchBaseSavePolicy = SAVE_AND_UPDATE) {
        val savedDocument = database.getDocument(document.id)?.toMutable() ?: MutableDocument(document.id)
        val databaseDocument =
            if(policy == SAVE_AND_UPDATE) savedDocument
            else MutableDocument(document.id)
        try {
            val data: HashMap<String, Any> =
                gson.fromJson(
                    gson.toJson(document),
                    object : TypeToken<HashMap<String, Any>>() {}.type
                )
            databaseDocument.apply {
                setValue(document.id, data)
            }
            database.save(MutableDocument(document.id, data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Save a list of documents in the database
     * @param documents list of documents to be saved
     * @param policy save policy, if SAVE_AND_UPDATE, documents with the same id will be overwritten
     * if CREATE_NEW_ALWAYS, documents with the same id will be created as new documents in the database
     */
    fun <T> save(documents: List<CouchBaseDocument<T>>, policy: CouchBaseSavePolicy = SAVE_AND_UPDATE) {
        database.inBatch {
            documents.forEach { document ->
                save(document, policy)
            }
        }
    }

    /**
     * Fetch all data in the database
     * @param whereExpression expression to filter the query
     * @param orderedBy array of Ordering objects, which are useful to sort the retrieved data
     * @param modelType type of the class saved in the database, this specify the returning type of the generic object
     * @return list of generic object
     */
    fun <T> fetchAll(whereExpression: Expression? = null, orderedBy: Array<Ordering>? = null, modelType: Class<T>): List<T> {
        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
        (query as From).let { fromQuery ->
            if(whereExpression != null) {
                query = fromQuery.where(whereExpression)
                (query as Where).let { whereQuery ->
                    if(orderedBy != null) {
                        query = whereQuery.orderBy(*orderedBy)
                    }
                }
            } else if(orderedBy != null) {
                query = fromQuery.orderBy(*orderedBy)
            }
        }
        return fetchQuery(query, modelType)
    }

    /**
     * Fetch one document based on the id of the input document.
     * @param document the document object which id will be taken to retrieve an object
     * @param modelType type of the class saved in the database, this specify the returning type of the generic object
     * @return list of generic object with size 1 if the searched object was found, otherwise size 0
     */
    fun <T> fetch(document: CouchBaseDocument<T>, modelType: Class<T>): List<T> {
        val databaseDocument = database.getDocument(document.id)
        val result = mutableListOf<T>()
        if(databaseDocument != null) {
            result.add(mapObjectToCouchBaseDocument(databaseDocument.toMap(), modelType).attributes)
        }
        return result
    }

    /**
     * Delete all documents in the database
     * @param whereExpression expression to filter the documents to be deleted
     * @param modelType type of the class saved in the database
     */
    fun <T> deleteAll(whereExpression: Expression? = null, modelType: Class<T>) {
        val fromQuery = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
        var whereQuery: Where? = null
        if(whereExpression != null) {
            whereQuery = fromQuery.where(whereExpression)
        }
        val query = whereQuery ?: fromQuery
        try {
            database.inBatch {
                (query as Query).execute().forEach { result ->
                    result.toMap()[databaseName]?.let { document ->
                        val couchbaseDocument = mapObjectToCouchBaseDocument(document, modelType)
                        database.delete(database.getDocument(couchbaseDocument.id))
                        //Log.d(TAG, "deleteAll: data: $couchbaseDocument")
                        //Log.d(TAG, "deleteAll: attributes: ${couchbaseDocument.attributes}")
                    }
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete one document of the database
     * @param document document to be deleted
     */
    fun <T> delete(document: CouchBaseDocument<T>) {
        val databaseDocument = database.getDocument(document.id) ?: return
        try {
            database.delete(databaseDocument)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete a list of documents of the database
     * @param documents list of documents to be deleted
     */
    fun <T> delete(documents: List<CouchBaseDocument<T>>) {
        try {
            database.inBatch {
                documents.forEach { document ->
                    delete(document)
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes the whole database
     */
    fun deleteDatabase() {
        database.delete()
    }

    companion object {
        private const val TAG = "DEBUG"
    }
}
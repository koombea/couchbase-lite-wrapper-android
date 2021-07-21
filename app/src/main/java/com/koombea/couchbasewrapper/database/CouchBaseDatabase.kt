package com.koombea.couchbasewrapper.database

import android.content.Context
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
    val databaseName: String,
    private val configuration: CouchBaseDatabaseConfiguration? = null
) {

    val gson: Gson by lazy { Gson() }

    init {
        setup()
    }

    lateinit var database: Database

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

    inline fun <reified T> fetchQuery(query: Query): List<T> {
        val results = mutableListOf<T>()
        database.inBatch {
            query.execute().forEach { result ->
                val dictionary = result.getDictionary(databaseName)
                dictionary?.let {
                    val id = it.getString("id")
                    if(id != null) {
                        val document = database.getDocument(id)
                        val data = mapObjectToCouchBaseDocument<T>(document).attributes
                        results.add(data)
                    }
                }
            }
        }
        return results
    }

    inline fun <reified T> mapObjectToCouchBaseDocument(document: Document): CouchBaseDocument<T> {
        //Log.d(TAG, "mapObjectToCouchBaseDocument: document: $document")
        //Log.d(TAG, "mapObjectToCouchBaseDocument: modelType: $modelType")
        val elementString = gson.toJson(document.toMap())
        val type = object : TypeToken<CouchBaseDocument<T>>() {}.type
        return gson.fromJson(elementString.toString(), type)
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
     * @return list of generic object
     */
    inline fun <reified T> fetchAll(whereExpression: Expression? = null, orderedBy: Array<Ordering>? = null): List<T> {
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
        return fetchQuery(query)
    }

    /**
     * Fetch one document based on the id of the input document.
     * @param document the document object which id will be taken to retrieve an object
     * @return list of generic object with size 1 if the searched object was found, otherwise size 0
     */
    inline fun <reified T> fetch(document: CouchBaseDocument<T>): List<T> {
        val databaseDocument = database.getDocument(document.id)
        val result = mutableListOf<T>()
        if(databaseDocument != null) {
            result.add(mapObjectToCouchBaseDocument<T>(databaseDocument).attributes)
        }
        return result
    }

    /**
     * Delete all documents in the database
     * @param whereExpression expression to filter the documents to be deleted
     */
    fun deleteAll(whereExpression: Expression? = null) {
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
                    val dictionary = result.getDictionary(databaseName)
                    dictionary?.let {
                        val id = it.getString("id")
                        if(id != null){
                            val document = database.getDocument(id)
                            database.delete(document)
                        }
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

    inline fun <reified T>  find(id: String): T? {
        val document: Document? = database.getDocument(id)
        return if(document != null) {
            val elementString = gson.toJson(document.toMap())
            val type = object : TypeToken<CouchBaseDocument<T>>() {}.type
            gson.fromJson<CouchBaseDocument<T>>(elementString.toString(), type).attributes
        } else {
            null
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
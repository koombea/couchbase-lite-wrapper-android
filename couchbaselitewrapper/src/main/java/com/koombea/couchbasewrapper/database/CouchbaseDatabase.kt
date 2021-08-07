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
package com.koombea.couchbasewrapper.database

import android.content.Context
import com.couchbase.lite.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import java.util.*

/**
 * Couchbase Lite Wrapper with basic CRUD
 * The intended usage is to save only one type of object in each database instance
 * If another kind of object would be saved, then create another CouchbaseDatabase to operate
 * with that object type
 */
class CouchbaseDatabase (
    private val context: Context,
    @PublishedApi internal val databaseName: String,
    private val configuration: CouchbaseDatabaseConfiguration? = null
) {

    @PublishedApi
    internal val gson: Gson by lazy { Gson() }
    @PublishedApi
    internal lateinit var database: Database

    init {
        setup()
    }

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

    @PublishedApi
    internal inline fun <reified T> mapObjectToCouchBaseDocument(document: Any): CouchbaseDocument<T> {
        val type = object : TypeToken<CouchbaseDocument<T>>() {}.type
        return gson.fromJson(gson.toJson(document), type)
    }

    /**
     * Save a document in the database
     * @param document document to be saved
     */
    fun <T> save(document: CouchbaseDocument<T>) {
        val databaseDocument = database.getDocument(document.id)?.toMutable() ?: MutableDocument(document.id)
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
     */
    fun <T> save(documents: List<CouchbaseDocument<T>>) {
        database.inBatch {
            documents.forEach { document ->
                save(document)
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
        val results = mutableListOf<T>()
        database.inBatch {
            query.execute().forEach { result ->
                result.toMap()[databaseName]?.let { document ->
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
        val databaseDocument = database.getDocument(id)
        if(databaseDocument != null) {
            return mapObjectToCouchBaseDocument<T>(databaseDocument.toMap()).attributes
        }
        return null
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
                    result.toMap()[databaseName]?.let { document ->
                        val type = object : TypeToken<CouchbaseDocument<Any>>() {}.type
                        val couchbaseDocument = gson.fromJson<CouchbaseDocument<Any>>(gson.toJson(document), type)
                        database.delete(database.getDocument(couchbaseDocument.id))
                    }
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete one document of the database
     * @param id id of the document to be deleted
     */
    fun delete(id: String) {
        val databaseDocument = database.getDocument(id) ?: return
        try {
            database.delete(databaseDocument)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete a list of documents of the database
     * @param idList list of ids of documents to be deleted
     */
    @JvmName("delete1")
    fun delete(idList: List<String>) {
        try {
            database.inBatch {
                idList.forEach { id ->
                    delete(id)
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
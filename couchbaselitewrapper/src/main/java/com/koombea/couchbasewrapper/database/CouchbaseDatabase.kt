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
import com.couchbase.lite.Collection

/**
 * Couchbase Lite Wrapper with basic CRUD
 * The intended usage is to save only one type of object in each database instance
 * If another kind of object would be saved, then create another CouchbaseDatabase to operate
 * with that object type
 */
class CouchbaseDatabase(
    private val context: Context,
    @PublishedApi internal val databaseName: String,
    private val configuration: CouchbaseDatabaseConfiguration? = null
) {

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

    /**
     * Creates a new collection
     * @param collectionName the name of the collection that is going to be created
     * @return new [Collection]
     */
    fun createCollection(collectionName: String): Collection {
       return database.createCollection(collectionName)
    }

    /**
     * Returns a collection from the database
     * @param collectionName the name of the collection that is going to be created
     */
    fun getCollection(collectionName: String): Collection? {
       return database.getCollection(collectionName)
    }

    /**
     * Deletes a collection
     * @param collectionName the name of the collection
     */
    fun deleteCollection(collectionName: String) {
        database.getCollection(collectionName)?.let {
            database.deleteCollection(collectionName)
        }
    }

    /**
     * Deletes the whole database
     */
    fun deleteDatabase() {
        database.delete()
    }
}
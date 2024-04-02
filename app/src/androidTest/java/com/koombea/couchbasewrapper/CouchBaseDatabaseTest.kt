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
package com.koombea.couchbasewrapper

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.koombea.couchbasewrapper.database.CouchbaseDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule

@SmallTest
@ExperimentalCoroutinesApi
class CouchBaseDatabaseTest {

    private lateinit var database: CouchbaseDatabase
    private lateinit var instrumentationContext: Context

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        database = CouchbaseDatabase(
            instrumentationContext,
            "TEST_DB"
        )
    }

    @Test
    fun createACollectionSuccessfully() {
        val expectedCollection = database.createCollection("Vehicle")
        val actualCollection = database.getCollection("Vehicle")
        assertEquals(expectedCollection.name(), actualCollection?.name())
    }

    @Test
    fun deleteACollectionSuccessfully() {
        val expectedCollection = database.createCollection("Vehicle")
        val actualCollection = database.getCollection("Vehicle")
        assertEquals(expectedCollection.name(), actualCollection?.name())
        database.deleteCollection("Vehicle")
        val deletedCollection = database.getCollection("Vehicle")
        assertEquals(null, deletedCollection)
    }

    @After
    fun teardown() {
        database.deleteDatabase()
    }
}
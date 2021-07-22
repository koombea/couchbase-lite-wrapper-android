package com.koombea.couchbasewrapper.database

data class CouchbaseDatabaseConfiguration(
    val databaseName: String,
    val databaseDirectory: String? = null
)
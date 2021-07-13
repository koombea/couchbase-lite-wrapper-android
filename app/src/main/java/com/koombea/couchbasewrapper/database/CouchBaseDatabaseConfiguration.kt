package com.koombea.couchbasewrapper.database

data class CouchBaseDatabaseConfiguration(
    val databaseName: String,
    val databaseDirectory: String? = null
)
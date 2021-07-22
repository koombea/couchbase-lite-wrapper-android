package com.koombea.couchbasewrapper.database

data class CouchbaseDocument <T>(
    val id: String,
    val attributes: T
)
